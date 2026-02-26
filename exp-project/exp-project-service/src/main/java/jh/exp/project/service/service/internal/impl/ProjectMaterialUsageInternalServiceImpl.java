package jh.exp.project.service.service.internal.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialUsage;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectMaterialUsageMapper;
import jh.exp.project.service.service.internal.ProjectMaterialUsageInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectMaterialUsageInternalServiceImpl implements ProjectMaterialUsageInternalService {
    private final ProjectMaterialUsageMapper mapper;

    @Override
    public SimplePageRes<ProjectMaterialUsage> list(SimplePageReq<Object> req) {
        Page<ProjectMaterialUsage> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ProjectMaterialUsage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectMaterialUsage::getUsageId);
        Page<ProjectMaterialUsage> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public ProjectMaterialUsage detail(Long usageId) {
        ProjectMaterialUsage entity = mapper.selectById(usageId);
        if (entity == null) {
            throw new RuntimeException("项目物料使用记录不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public ProjectMaterialUsage create(ProjectMaterialUsage req) {
        mapper.insert(req);
        return detail(req.getUsageId());
    }

    @Override
    @Transactional
    public ProjectMaterialUsage update(ProjectMaterialUsage req) {
        if (req.getUsageId() == null) {
            throw new RuntimeException("usageId不能为空");
        }
        ProjectMaterialUsage old = detail(req.getUsageId());
        BeanUtils.copyProperties(req, old, "usageId");
        mapper.updateById(old);
        return detail(old.getUsageId());
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
