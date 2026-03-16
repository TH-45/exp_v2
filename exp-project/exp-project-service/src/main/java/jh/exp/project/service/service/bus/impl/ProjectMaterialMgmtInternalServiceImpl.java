package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.ProjectMaterialPlan;
import jh.exp.project.core.entity.ProjectMaterialStock;
import jh.exp.project.core.entity.ProjectMaterialUsage;
import jh.exp.project.core.entity.req.ProjectMaterialCreateReq;
import jh.exp.project.core.entity.req.ProjectMaterialDeleteReq;
import jh.exp.project.core.entity.req.ProjectMaterialInboundReq;
import jh.exp.project.core.entity.req.ProjectMaterialOutboundReq;
import jh.exp.project.core.entity.req.ProjectMaterialUpdateReq;
import jh.exp.project.core.entity.res.ProjectMaterialDetailRes;
import jh.exp.project.core.entity.res.ProjectMaterialRes;
import jh.exp.project.core.mapper.ProjectMapper;
import jh.exp.project.core.mapper.ProjectMaterialPlanMapper;
import jh.exp.project.core.mapper.ProjectMaterialStockMapper;
import jh.exp.project.core.mapper.ProjectMaterialUsageMapper;
import jh.exp.project.service.service.bus.ProjectMaterialMgmtInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMaterialMgmtInternalServiceImpl implements ProjectMaterialMgmtInternalService {
    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_LOW = "LOW";
    private static final String STATUS_OUT = "OUT";

    private final ProjectMaterialStockMapper stockMapper;
    private final ProjectMaterialPlanMapper planMapper;
    private final ProjectMaterialUsageMapper usageMapper;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectMaterialDetailRes detail(Long projectId) {
        checkProjectExists(projectId);
        List<ProjectMaterialStock> stocks = listStockByProject(projectId);
        List<ProjectMaterialPlan> plans = listPlanByProject(projectId);
        List<ProjectMaterialUsage> usages = listUsageByProject(projectId);

        Map<String, BigDecimal> requiredMap = plans.stream().collect(Collectors.groupingBy(
                this::materialKey,
                LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, p -> nvl(p.getPlanQty()), BigDecimal::add)
        ));
        Map<String, BigDecimal> usedMap = usages.stream().collect(Collectors.groupingBy(
                this::materialKey,
                LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, u -> nvl(u.getQty()), BigDecimal::add)
        ));

        List<ProjectMaterialRes> materialRes = new ArrayList<>(stocks.size());
        for (ProjectMaterialStock stock : stocks) {
            recalcStockStatus(stock);
            ProjectMaterialRes row = toRes(stock);
            String key = materialKey(stock);
            row.setRequiredQuantity(scale(requiredMap.getOrDefault(key, BigDecimal.ZERO)));
            row.setUsedQuantity(scale(usedMap.getOrDefault(key, BigDecimal.ZERO)));
            row.setTotalAmount(scale(row.getRequiredQuantity().multiply(nvl(row.getUnitPrice()))));
            materialRes.add(row);
        }
        int lowCount = (int) materialRes.stream().filter(r -> "LOW_STOCK".equals(r.getStatus())).count();
        int outCount = (int) materialRes.stream().filter(r -> "OUT_OF_STOCK".equals(r.getStatus())).count();

        ProjectMaterialDetailRes res = new ProjectMaterialDetailRes();
        res.setMaterials(materialRes);
        res.setTotal(materialRes.size());
        res.setLowStock(lowCount);
        res.setOutOfStock(outCount);
        return res;
    }

    @Override
    @Transactional
    public ProjectMaterialRes create(ProjectMaterialCreateReq req) {
        checkProjectExists(req.getProjectId());
        validatePositive(req.getRequiredQuantity(), "需求量必须大于等于0");
        validatePositive(req.getUnitPrice(), "单价必须大于等于0");

        String materialCode = req.getMaterialCode().trim();
        String spec = req.getSpec().trim();
        ensureStockUnique(req.getProjectId(), materialCode, spec, null);

        ProjectMaterialStock stock = new ProjectMaterialStock();
        stock.setProjectId(req.getProjectId());
        stock.setMaterialCode(materialCode);
        stock.setMaterialName(req.getMaterialName());
        stock.setSpec(spec);
        stock.setUnit(req.getUnit());
        stock.setStockQty(BigDecimal.ZERO);
        stock.setReceivedQty(BigDecimal.ZERO);
        stock.setUnitPrice(scale(req.getUnitPrice()));
        stock.setSupplierName(req.getSupplierName());
        stock.setSafeStockQty(scale(defaultSafeQty(req.getSafeStockQty(), req.getRequiredQuantity())));
        stock.setLocation(null);
        stock.setUpdatedTime(LocalDateTime.now());
        recalcStockStatus(stock);
        stockMapper.insert(stock);

        ProjectMaterialPlan plan = findOrCreatePlan(stock, req.getRequiredQuantity());
        planMapper.insert(plan);

        return buildMaterialRes(stock);
    }

    @Override
    @Transactional
    public ProjectMaterialRes update(ProjectMaterialUpdateReq req) {
        ProjectMaterialStock stock = requireStock(req.getId());
        validatePositive(req.getRequiredQuantity(), "需求量必须大于等于0");
        validatePositive(req.getUnitPrice(), "单价必须大于等于0");

        ensureStockUnique(stock.getProjectId(), stock.getMaterialCode(), req.getSpec().trim(), stock.getStockId());
        stock.setMaterialName(req.getMaterialName());
        stock.setSpec(req.getSpec().trim());
        stock.setUnit(req.getUnit());
        stock.setUnitPrice(scale(req.getUnitPrice()));
        stock.setSupplierName(req.getSupplierName());
        stock.setSafeStockQty(scale(defaultSafeQty(req.getSafeStockQty(), req.getRequiredQuantity())));
        stock.setUpdatedTime(LocalDateTime.now());
        recalcStockStatus(stock);
        stockMapper.updateById(stock);

        ProjectMaterialPlan plan = findPlan(stock.getProjectId(), stock.getMaterialCode(), stock.getSpec());
        if (plan != null) {
            plan.setMaterialName(stock.getMaterialName());
            plan.setSpec(stock.getSpec());
            plan.setUnit(stock.getUnit());
            plan.setPlanQty(scale(req.getRequiredQuantity()));
            plan.setUpdatedTime(LocalDateTime.now());
            planMapper.updateById(plan);
        } else {
            ProjectMaterialPlan newPlan = findOrCreatePlan(stock, req.getRequiredQuantity());
            planMapper.insert(newPlan);
        }

        return buildMaterialRes(stock);
    }

    @Override
    @Transactional
    public void delete(ProjectMaterialDeleteReq req) {
        ProjectMaterialStock stock = requireStock(req.getId());
        if (hasUsageRecord(stock.getProjectId(), stock.getMaterialCode(), stock.getSpec())) {
            throw new RuntimeException("该物料已存在出库记录，禁止删除");
        }
        stockMapper.deleteById(stock.getStockId());
        ProjectMaterialPlan plan = findPlan(stock.getProjectId(), stock.getMaterialCode(), stock.getSpec());
        if (plan != null) {
            planMapper.deleteById(plan.getPlanId());
        }
    }

    @Override
    @Transactional
    public ProjectMaterialRes inbound(ProjectMaterialInboundReq req) {
        ProjectMaterialStock stock = requireStock(req.getId());
        validateGtZero(req.getQuantity(), "入库数量必须大于0");
        BigDecimal qty = scale(req.getQuantity());
        stock.setReceivedQty(scale(nvl(stock.getReceivedQty()).add(qty)));
        stock.setStockQty(scale(nvl(stock.getStockQty()).add(qty)));
        stock.setUpdatedTime(LocalDateTime.now());
        recalcStockStatus(stock);
        stockMapper.updateById(stock);
        return buildMaterialRes(stock);
    }

    @Override
    @Transactional
    public ProjectMaterialRes outbound(ProjectMaterialOutboundReq req) {
        ProjectMaterialStock stock = requireStock(req.getId());
        validateGtZero(req.getQuantity(), "出库数量必须大于0");
        BigDecimal qty = scale(req.getQuantity());
        if (nvl(stock.getStockQty()).compareTo(qty) < 0) {
            throw new RuntimeException("出库数量不能超过库存");
        }

        stock.setStockQty(scale(nvl(stock.getStockQty()).subtract(qty)));
        stock.setUpdatedTime(LocalDateTime.now());
        recalcStockStatus(stock);
        stockMapper.updateById(stock);

        ProjectMaterialUsage usage = new ProjectMaterialUsage();
        usage.setProjectId(stock.getProjectId());
        usage.setMaterialCode(stock.getMaterialCode());
        usage.setMaterialName(stock.getMaterialName());
        usage.setSpec(stock.getSpec());
        usage.setUnit(stock.getUnit());
        usage.setQty(qty);
        usage.setUseDate(req.getUseDate() == null ? LocalDate.now() : req.getUseDate());
        usage.setOperatorUserId(currentUserId());
        usage.setRemark(req.getRemarks());
        usageMapper.insert(usage);

        return buildMaterialRes(stock);
    }

    private void checkProjectExists(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
    }

    private ProjectMaterialStock requireStock(Long id) {
        ProjectMaterialStock stock = stockMapper.selectById(id);
        if (stock == null) {
            throw new RuntimeException("物料库存不存在");
        }
        return stock;
    }

    private void ensureStockUnique(Long projectId, String materialCode, String spec, Long excludeId) {
        LambdaQueryWrapper<ProjectMaterialStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMaterialStock::getProjectId, projectId)
                .eq(ProjectMaterialStock::getMaterialCode, materialCode)
                .eq(ProjectMaterialStock::getSpec, spec);
        if (excludeId != null) {
            wrapper.ne(ProjectMaterialStock::getStockId, excludeId);
        }
        wrapper.last("limit 1");
        ProjectMaterialStock exists = stockMapper.selectOne(wrapper);
        if (exists != null) {
            throw new RuntimeException("同项目下物料编码+规格已存在");
        }
    }

    private List<ProjectMaterialStock> listStockByProject(Long projectId) {
        LambdaQueryWrapper<ProjectMaterialStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMaterialStock::getProjectId, projectId)
                .orderByDesc(ProjectMaterialStock::getUpdatedTime)
                .orderByDesc(ProjectMaterialStock::getStockId);
        return stockMapper.selectList(wrapper);
    }

    private List<ProjectMaterialPlan> listPlanByProject(Long projectId) {
        LambdaQueryWrapper<ProjectMaterialPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMaterialPlan::getProjectId, projectId);
        return planMapper.selectList(wrapper);
    }

    private List<ProjectMaterialUsage> listUsageByProject(Long projectId) {
        LambdaQueryWrapper<ProjectMaterialUsage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMaterialUsage::getProjectId, projectId);
        return usageMapper.selectList(wrapper);
    }

    private ProjectMaterialPlan findPlan(Long projectId, String materialCode, String spec) {
        LambdaQueryWrapper<ProjectMaterialPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMaterialPlan::getProjectId, projectId)
                .eq(ProjectMaterialPlan::getMaterialCode, materialCode)
                .eq(ProjectMaterialPlan::getSpec, spec)
                .orderByDesc(ProjectMaterialPlan::getPlanId)
                .last("limit 1");
        return planMapper.selectOne(wrapper);
    }

    private ProjectMaterialPlan findOrCreatePlan(ProjectMaterialStock stock, BigDecimal requiredQty) {
        ProjectMaterialPlan plan = new ProjectMaterialPlan();
        plan.setProjectId(stock.getProjectId());
        plan.setMaterialCode(stock.getMaterialCode());
        plan.setMaterialName(stock.getMaterialName());
        plan.setSpec(stock.getSpec());
        plan.setUnit(stock.getUnit());
        plan.setPlanQty(scale(requiredQty));
        plan.setStatus("PLANNED");
        plan.setCreatedBy(currentUserId());
        plan.setCreatedTime(LocalDateTime.now());
        plan.setUpdatedTime(LocalDateTime.now());
        return plan;
    }

    private ProjectMaterialRes buildMaterialRes(ProjectMaterialStock stock) {
        ProjectMaterialRes res = toRes(stock);
        ProjectMaterialPlan plan = findPlan(stock.getProjectId(), stock.getMaterialCode(), stock.getSpec());
        BigDecimal required = plan == null ? BigDecimal.ZERO : nvl(plan.getPlanQty());
        BigDecimal used = sumUsed(stock.getProjectId(), stock.getMaterialCode(), stock.getSpec());
        res.setRequiredQuantity(scale(required));
        res.setUsedQuantity(scale(used));
        res.setTotalAmount(scale(required.multiply(nvl(res.getUnitPrice()))));
        return res;
    }

    private BigDecimal sumUsed(Long projectId, String materialCode, String spec) {
        LambdaQueryWrapper<ProjectMaterialUsage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMaterialUsage::getProjectId, projectId)
                .eq(ProjectMaterialUsage::getMaterialCode, materialCode)
                .eq(ProjectMaterialUsage::getSpec, spec);
        List<ProjectMaterialUsage> list = usageMapper.selectList(wrapper);
        return list.stream().map(ProjectMaterialUsage::getQty).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasUsageRecord(Long projectId, String materialCode, String spec) {
        LambdaQueryWrapper<ProjectMaterialUsage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMaterialUsage::getProjectId, projectId)
                .eq(ProjectMaterialUsage::getMaterialCode, materialCode)
                .eq(ProjectMaterialUsage::getSpec, spec)
                .last("limit 1");
        return usageMapper.selectOne(wrapper) != null;
    }

    private ProjectMaterialRes toRes(ProjectMaterialStock stock) {
        ProjectMaterialRes res = new ProjectMaterialRes();
        res.setId(stock.getStockId());
        res.setProjectId(stock.getProjectId());
        res.setMaterialCode(stock.getMaterialCode());
        res.setMaterialName(stock.getMaterialName());
        res.setSpec(stock.getSpec());
        res.setUnit(stock.getUnit());
        res.setReceivedQuantity(scale(nvl(stock.getReceivedQty())));
        res.setStockQuantity(scale(nvl(stock.getStockQty())));
        res.setUnitPrice(scale(nvl(stock.getUnitPrice())));
        res.setSupplierName(stock.getSupplierName());
        res.setStatus(toUiStatus(stock.getStatus()));
        res.setLastUpdateTime(stock.getUpdatedTime());
        return res;
    }

    private void recalcStockStatus(ProjectMaterialStock stock) {
        BigDecimal stockQty = nvl(stock.getStockQty());
        BigDecimal safeQty = nvl(stock.getSafeStockQty());
        if (stockQty.compareTo(BigDecimal.ZERO) <= 0) {
            stock.setStatus(STATUS_OUT);
            return;
        }
        if (stockQty.compareTo(safeQty) <= 0) {
            stock.setStatus(STATUS_LOW);
            return;
        }
        stock.setStatus(STATUS_NORMAL);
    }

    private String toUiStatus(String dbStatus) {
        if (STATUS_OUT.equalsIgnoreCase(dbStatus)) {
            return "OUT_OF_STOCK";
        }
        if (STATUS_LOW.equalsIgnoreCase(dbStatus)) {
            return "LOW_STOCK";
        }
        return "NORMAL";
    }

    private void validatePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(message);
        }
    }

    private void validateGtZero(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(message);
        }
    }

    private BigDecimal defaultSafeQty(BigDecimal safeStockQty, BigDecimal requiredQty) {
        if (safeStockQty != null) {
            return safeStockQty;
        }
        return nvl(requiredQty).multiply(BigDecimal.valueOf(0.1));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String materialKey(ProjectMaterialPlan plan) {
        return plan.getMaterialCode() + "||" + plan.getSpec();
    }

    private String materialKey(ProjectMaterialUsage usage) {
        return usage.getMaterialCode() + "||" + usage.getSpec();
    }

    private String materialKey(ProjectMaterialStock stock) {
        return stock.getMaterialCode() + "||" + stock.getSpec();
    }

    private Long currentUserId() {
        CurrentUser currentUser = CurrentUserHolder.get();
        return currentUser == null ? null : currentUser.getUserId();
    }
}
