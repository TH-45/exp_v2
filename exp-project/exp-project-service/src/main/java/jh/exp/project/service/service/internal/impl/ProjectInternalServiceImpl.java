package jh.exp.project.service.service.internal.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.constant.CommonConstant;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectMapper;
import jh.exp.project.service.service.internal.ProjectInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectInternalServiceImpl implements ProjectInternalService {
    private final ProjectMapper projectMapper;

    @Override
    public SimplePageRes<Project> list(SimplePageReq<Object> req) {
        Page<Project> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Project::getProjectId);
        Page<Project> result = projectMapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public Project detail(Long projectId) {
        Project entity = projectMapper.selectById(projectId);
        if (entity == null) {
            throw new RuntimeException("项目不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public Project create(Project req) {
        req.setCreatedTime(LocalDateTime.now());
        req.setUpdatedTime(LocalDateTime.now());
        projectMapper.insert(req);
        return detail(req.getProjectId());
    }

    @Override
    @Transactional
    public Project update(Project req) {
        if (req.getProjectId() == null) {
            throw new RuntimeException("projectId不能为空");
        }
        Project old = detail(req.getProjectId());
        BeanUtils.copyProperties(req, old, "projectId", "createdTime");
        old.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(old);
        return detail(old.getProjectId());
    }

    @Override
    @Transactional
    public void delete(DeleteByIdReq req) {
        detail(req.getId());
        projectMapper.deleteById(req.getId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteByIdsReq req) {
        if (req.getIds() == null || req.getIds().isEmpty()) {
            return;
        }
        projectMapper.deleteBatchIds(req.getIds());
    }

    @Override
    public ApiResponse<Map<Long, Project>> batchGetProjectByIds(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return ApiResponse.success(null);
        }
        List<Project> projects = projectMapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getProjectStatus, CommonConstant.ENABLED_STATUS_STR)
                .in(Project::getProjectId, projectIds));

        Map<Long, Project> projectCache = projects.stream().collect(
                Collectors.toMap(Project::getProjectId, project -> project)
        );
        return ApiResponse.success(projectCache);

    }


}
