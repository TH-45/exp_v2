package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialPlan;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectMaterialPlanMapper;
import jh.exp.project.service.service.bus.ProjectMaterialPlanInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectMaterialPlanInternalServiceImpl implements ProjectMaterialPlanInternalService {
    private final ProjectMaterialPlanMapper mapper;

    @Override
    public SimplePageRes<ProjectMaterialPlan> list(SimplePageReq<Object> req) {
        Page<ProjectMaterialPlan> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ProjectMaterialPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectMaterialPlan::getPlanId);
        Page<ProjectMaterialPlan> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public ProjectMaterialPlan detail(Long planId) {
        ProjectMaterialPlan entity = mapper.selectById(planId);
        if (entity == null) {
            throw new RuntimeException("项目物料计划不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public ProjectMaterialPlan create(ProjectMaterialPlan req) {
        req.setCreatedTime(LocalDateTime.now());
        req.setUpdatedTime(LocalDateTime.now());
        mapper.insert(req);
        return detail(req.getPlanId());
    }

    @Override
    @Transactional
    public ProjectMaterialPlan update(ProjectMaterialPlan req) {
        if (req.getPlanId() == null) {
            throw new RuntimeException("planId不能为空");
        }
        ProjectMaterialPlan old = detail(req.getPlanId());
        BeanUtils.copyProperties(req, old, "planId", "createdTime");
        old.setUpdatedTime(LocalDateTime.now());
        mapper.updateById(old);
        return detail(old.getPlanId());
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
