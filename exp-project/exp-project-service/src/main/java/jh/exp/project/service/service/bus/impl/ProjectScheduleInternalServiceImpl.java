package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectSchedule;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectScheduleMapper;
import jh.exp.project.service.service.bus.ProjectScheduleInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectScheduleInternalServiceImpl implements ProjectScheduleInternalService {
    private final ProjectScheduleMapper mapper;

    @Override
    public SimplePageRes<ProjectSchedule> list(SimplePageReq<Object> req) {
        Page<ProjectSchedule> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ProjectSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectSchedule::getScheduleId);
        Page<ProjectSchedule> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public ProjectSchedule detail(Long scheduleId) {
        ProjectSchedule entity = mapper.selectById(scheduleId);
        if (entity == null) {
            throw new RuntimeException("项目进度计划不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public ProjectSchedule create(ProjectSchedule req) {
        mapper.insert(req);
        return detail(req.getScheduleId());
    }

    @Override
    @Transactional
    public ProjectSchedule update(ProjectSchedule req) {
        if (req.getScheduleId() == null) {
            throw new RuntimeException("scheduleId不能为空");
        }
        ProjectSchedule old = detail(req.getScheduleId());
        BeanUtils.copyProperties(req, old, "scheduleId");
        mapper.updateById(old);
        return detail(old.getScheduleId());
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
