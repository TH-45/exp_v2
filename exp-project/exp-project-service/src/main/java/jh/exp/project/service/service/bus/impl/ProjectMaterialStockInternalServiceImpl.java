package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialStock;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectMaterialStockMapper;
import jh.exp.project.service.service.bus.ProjectMaterialStockInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectMaterialStockInternalServiceImpl implements ProjectMaterialStockInternalService {
    private final ProjectMaterialStockMapper mapper;

    @Override
    public SimplePageRes<ProjectMaterialStock> list(SimplePageReq<Object> req) {
        Page<ProjectMaterialStock> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ProjectMaterialStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectMaterialStock::getStockId);
        Page<ProjectMaterialStock> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public ProjectMaterialStock detail(Long stockId) {
        ProjectMaterialStock entity = mapper.selectById(stockId);
        if (entity == null) {
            throw new RuntimeException("项目物料库存不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public ProjectMaterialStock create(ProjectMaterialStock req) {
        req.setUpdatedTime(LocalDateTime.now());
        mapper.insert(req);
        return detail(req.getStockId());
    }

    @Override
    @Transactional
    public ProjectMaterialStock update(ProjectMaterialStock req) {
        if (req.getStockId() == null) {
            throw new RuntimeException("stockId不能为空");
        }
        ProjectMaterialStock old = detail(req.getStockId());
        BeanUtils.copyProperties(req, old, "stockId");
        old.setUpdatedTime(LocalDateTime.now());
        mapper.updateById(old);
        return detail(old.getStockId());
    }

    @Override
    @Transactional
    public void delete(DeleteByIdReq req) {
        detail(req.getId());
        mapper.deleteById(req.getId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteByIdsReq req) {
        if (req.getIds() == null || req.getIds().isEmpty()) {
            return;
        }
        mapper.deleteBatchIds(req.getIds());
    }
}
