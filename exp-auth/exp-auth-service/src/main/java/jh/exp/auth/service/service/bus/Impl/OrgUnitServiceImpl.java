package jh.exp.auth.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.core.entity.OrgUnit;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.OrgUnitListRes;
import jh.exp.auth.core.entity.res.OrgUnitTreeRes;
import jh.exp.auth.service.service.bus.OrgUnitService;





import jh.exp.auth.core.mapper.OrgUnitMapper;

import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.exception.BizException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jh.exp.auth.core.util.OrgTreeUtil;

/**
 * 组织服务实现类
 */
@Service
@RequiredArgsConstructor
public class OrgUnitServiceImpl implements OrgUnitService {

    private final OrgUnitMapper orgUnitMapper;

    /**
     * 分页查询组织列表
     */
    @Override
    public SimplePageRes<OrgUnitListRes> queryOrgUnitList(SimplePageReq<QueryOrgUnitReq> req) {
        req.pageDefault();
        Page<OrgUnitListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryOrgUnitReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryOrgUnitReq();
        }

        IPage<OrgUnitListRes> result = orgUnitMapper.selectOrgUnitList(page,
                queryParam.getOrgCode(),
                queryParam.getOrgName());

        SimplePageRes<OrgUnitListRes> pageRes = new SimplePageRes<>();
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setList(result.getRecords());
        return pageRes;
    }

    /**
     * 查询组织树
     */
    @Override
    public List<OrgUnitTreeRes> queryOrgUnitTree(QueryOrgUnitReq req) {
        if (req == null) {
            req = new QueryOrgUnitReq();
        }

        List<OrgUnit> allOrgs = orgUnitMapper.selectList(new LambdaQueryWrapper<OrgUnit>()
                .like(StringUtils.hasText(req.getOrgCode()), OrgUnit::getOrgCode, req.getOrgCode())
                .like(StringUtils.hasText(req.getOrgName()), OrgUnit::getOrgName, req.getOrgName())
                .eq(OrgUnit::getStatus, "ENABLED")
                .orderByAsc(OrgUnit::getSortNo)
                .orderByAsc(OrgUnit::getCreatedTime));

        return OrgTreeUtil.buildOrgTree(allOrgs, null);
    }

    /**
     * 根据ID查询组织详情
     */
    @Override
    public OrgUnitDetailRes getOrgUnitById(Long orgId) {
        OrgUnit orgUnit = orgUnitMapper.selectById(orgId);
        if (orgUnit == null) {
            throw new BizException("组织不存在");
        }
        OrgUnitDetailRes result = new OrgUnitDetailRes();
        BeanUtils.copyProperties(orgUnit, result);
        return result;
    }

    @Override
    public Map<Long, OrgUnitDetailRes> batchGetOrgUnitByIds(List<Long> orgIds) {
        Map<Long, OrgUnitDetailRes> result = new LinkedHashMap<>();
        if (orgIds == null || orgIds.isEmpty()) {
            return result;
        }
        List<OrgUnit> orgUnits = orgUnitMapper.selectList(new LambdaQueryWrapper<OrgUnit>()
            .in(OrgUnit::getOrgId, orgIds));
        for (OrgUnit orgUnit : orgUnits) {
            OrgUnitDetailRes detailRes = new OrgUnitDetailRes();
            BeanUtils.copyProperties(orgUnit, detailRes);
            result.put(orgUnit.getOrgId(), detailRes);
        }
        return result;
    }

    /**
     * 创建组织
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrgUnitDetailRes createOrgUnit(CreateOrgUnitReq req) {
        // 检查组织编码是否已存在
        if (checkOrgCodeExists(req.getOrgCode(), null)) {
            throw new BizException("组织编码已存在");
        }

        // 检查上级组织是否存在
        if (req.getParentOrgId() != null && req.getParentOrgId() > 0) {
            OrgUnit parentOrg = orgUnitMapper.selectById(req.getParentOrgId());
            if (parentOrg == null) {
                throw new BizException("上级组织不存在");
            }
        }

        // 创建组织实体
        OrgUnit orgUnit = new OrgUnit();
        BeanUtils.copyProperties(req, orgUnit);
        orgUnit.setStatus("ENABLED");
        if (orgUnit.getSortNo() == null) {
            orgUnit.setSortNo(0);
        }

        // 设置创建者信息
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser != null && currentUser.getUserId() != null) {
            orgUnit.setCreatedBy(Long.valueOf(currentUser.getUserId()));
        }
        orgUnit.setCreatedTime(LocalDateTime.now());
        orgUnit.setUpdatedTime(LocalDateTime.now());

        // 保存组织
        orgUnitMapper.insert(orgUnit);

        // 更新组织路径和层级
        updateOrgPathAndLevel(orgUnit);

        return getOrgUnitById(orgUnit.getOrgId());
    }

    /**
     * 更新组织
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrgUnitDetailRes updateOrgUnit(UpdateOrgUnitReq req) {
        // 检查组织是否存在
        OrgUnit existingOrg = orgUnitMapper.selectById(req.getOrgId());
        if (existingOrg == null) {
            throw new BizException("组织不存在");
        }

        // 检查组织编码是否已存在
        if (checkOrgCodeExists(req.getOrgCode(), req.getOrgId())) {
            throw new BizException("组织编码已存在");
        }

        // 检查上级组织是否存在
        if (req.getParentOrgId() != null && req.getParentOrgId() > 0) {
            OrgUnit parentOrg = orgUnitMapper.selectById(req.getParentOrgId());
            if (parentOrg == null) {
                throw new BizException("上级组织不存在");
            }
            // 防止将组织设置为自己的子组织
            if (existingOrg.getOrgPath() != null && existingOrg.getOrgPath().contains("/" + req.getParentOrgId() + "/")) {
                throw new BizException("不能将组织设置为自己的子组织");
            }
        }

        // 更新组织信息
        OrgUnit orgUnit = new OrgUnit();
        BeanUtils.copyProperties(req, orgUnit);
        orgUnit.setUpdatedTime(LocalDateTime.now());

        orgUnitMapper.updateById(orgUnit);

        // 如果上级组织发生变化，需要更新路径和层级
        if (!existingOrg.getParentOrgId().equals(req.getParentOrgId())) {
            updateOrgPathAndLevel(orgUnitMapper.selectById(req.getOrgId()));
        }

        return getOrgUnitById(req.getOrgId());
    }

    /**
     * 删除组织
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrgUnit(Long orgId) {
        deleteOrgUnitInternal(orgId);
    }

    /**
     * 批量删除组织
     * 注意：采用"全部成功或全部失败"的策略，如果任何一个删除失败，整个批量操作都会回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteOrgUnits(BatchDeleteOrgUnitReq req) {
        for (Long orgId : req.getOrgIds()) {
            deleteOrgUnitInternal(orgId);
        }
    }

    /**
     * 内部删除组织方法（不带事务注解，避免事务嵌套问题）
     */
    private void deleteOrgUnitInternal(Long orgId) {
        OrgUnit orgUnit = orgUnitMapper.selectById(orgId);
        if (orgUnit == null) {
            throw new BizException("组织不存在");
        }

        // 检查是否有子组织
        Long childCount = orgUnitMapper.selectCount(new LambdaQueryWrapper<OrgUnit>()
                .eq(OrgUnit::getParentOrgId, orgId));
        if (childCount > 0) {
            throw new BizException("该组织下有子组织，不能删除");
        }

        // 检查是否有人员属于该组织（这里可能需要关联人员表，暂时跳过）

        orgUnitMapper.deleteById(orgId);
    }

    /**
     * 更改组织状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrgUnitDetailRes updateOrgUnitStatus(OrgUnitStatusReq req) {
        OrgUnit orgUnit = orgUnitMapper.selectById(req.getOrgId());
        if (orgUnit == null) {
            throw new BizException("组织不存在");
        }

        // 验证状态值
        if (!"ENABLED".equals(req.getStatus()) && !"DISABLED".equals(req.getStatus())) {
            throw new BizException("无效的状态值");
        }

        orgUnit.setStatus(req.getStatus());
        orgUnit.setUpdatedTime(LocalDateTime.now());
        orgUnitMapper.updateById(orgUnit);

        return getOrgUnitById(req.getOrgId());
    }

    /**
     * 批量更改组织状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateOrgUnitStatus(BatchOrgUnitStatusReq req) {
        // 验证状态值
        if (!"ENABLED".equals(req.getStatus()) && !"DISABLED".equals(req.getStatus())) {
            throw new BizException("无效的状态值");
        }

        UpdateWrapper<OrgUnit> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("org_id", req.getOrgIds())
                    .set("status", req.getStatus())
                    .set("updated_time", LocalDateTime.now());

        orgUnitMapper.update(null, updateWrapper);
    }

    /**
     * 移动组织（更改组织树结构）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrgUnitDetailRes moveOrgUnit(MoveOrgUnitReq req) {
        OrgUnit orgUnit = orgUnitMapper.selectById(req.getOrgId());
        if (orgUnit == null) {
            throw new BizException("组织不存在");
        }

        // 检查目标上级组织是否存在
        if (req.getTargetParentOrgId() != null && req.getTargetParentOrgId() > 0) {
            OrgUnit targetParentOrg = orgUnitMapper.selectById(req.getTargetParentOrgId());
            if (targetParentOrg == null) {
                throw new BizException("目标上级组织不存在");
            }
            // 防止将组织设置为自己的子组织
            if (orgUnit.getOrgPath() != null && orgUnit.getOrgPath().contains("/" + req.getTargetParentOrgId() + "/")) {
                throw new BizException("不能将组织移动到自己的子组织下");
            }
        }

        orgUnit.setParentOrgId(req.getTargetParentOrgId());
        if (req.getSortNo() != null) {
            orgUnit.setSortNo(req.getSortNo());
        }
        orgUnit.setUpdatedTime(LocalDateTime.now());

        orgUnitMapper.updateById(orgUnit);
        updateOrgPathAndLevel(orgUnit);

        return getOrgUnitById(req.getOrgId());
    }

    /**
     * 检查组织编码是否存在
     */
    @Override
    public boolean checkOrgCodeExists(String orgCode, Long excludeOrgId) {
        return orgUnitMapper.countByOrgCode(orgCode, excludeOrgId) > 0;
    }



    /**
     * 更新组织路径和层级
     */
    private void updateOrgPathAndLevel(OrgUnit orgUnit) {
        String orgPath;
        Integer orgLevel;

        if (orgUnit.getParentOrgId() == null || orgUnit.getParentOrgId() == 0) {
            // 根节点
            orgPath = "/" + orgUnit.getOrgId() + "/";
            orgLevel = 1;
        } else {
            // 非根节点
            OrgUnit parentOrg = orgUnitMapper.selectById(orgUnit.getParentOrgId());
            if (parentOrg != null && parentOrg.getOrgPath() != null) {
                orgPath = parentOrg.getOrgPath() + orgUnit.getOrgId() + "/";
                orgLevel = parentOrg.getOrgLevel() + 1;
            } else {
                // 如果父节点不存在或路径为空，设置为根节点
                orgPath = "/" + orgUnit.getOrgId() + "/";
                orgLevel = 1;
            }
        }

        orgUnit.setOrgPath(orgPath);
        orgUnit.setOrgLevel(orgLevel);
        orgUnitMapper.updateById(orgUnit);

        // 递归更新所有子节点
        updateChildrenPathAndLevel(orgUnit.getOrgId(), orgPath, orgLevel);
    }

    /**
     * 递归更新子节点路径和层级
     */
    private void updateChildrenPathAndLevel(Long parentId, String parentPath, Integer parentLevel) {
        List<OrgUnit> children = orgUnitMapper.selectList(new LambdaQueryWrapper<OrgUnit>()
                .eq(OrgUnit::getParentOrgId, parentId));

        for (OrgUnit child : children) {
            String childPath = parentPath + child.getOrgId() + "/";
            Integer childLevel = parentLevel + 1;

            child.setOrgPath(childPath);
            child.setOrgLevel(childLevel);
            orgUnitMapper.updateById(child);

            // 递归更新子节点
            updateChildrenPathAndLevel(child.getOrgId(), childPath, childLevel);
        }
    }
}