package jh.exp.project.service.service.internal.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectScheduleLog;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectScheduleLogMapper;
import jh.exp.project.service.service.internal.ProjectScheduleLogInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectScheduleLogInternalServiceImpl implements ProjectScheduleLogInternalService {
    private final ProjectScheduleLogMapper mapper;

    @Override
    public SimplePageRes<ProjectScheduleLog> list(SimplePageReq<Object> req) {
        Page<ProjectScheduleLog> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ProjectScheduleLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectScheduleLog::getLogId);
        Page<ProjectScheduleLog> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public ProjectScheduleLog detail(Long logId) {
        ProjectScheduleLog entity = mapper.selectById(logId);
        if (entity == null) {
            throw new RuntimeException("项目进度日志不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public ProjectScheduleLog create(ProjectScheduleLog req) {
        mapper.insert(req);
        return detail(req.getLogId());
    }

    @Override
    @Transactional
    public ProjectScheduleLog update(ProjectScheduleLog req) {
        if (req.getLogId() == null) {
            throw new RuntimeException("logId不能为空");
        }
        ProjectScheduleLog old = detail(req.getLogId());
        BeanUtils.copyProperties(req, old, "logId");
        mapper.updateById(old);
        return detail(old.getLogId());
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
