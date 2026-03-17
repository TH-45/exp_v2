package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.req.QueryPersonReq;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.entity.res.PersonInfoRes;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.constant.CommonConstant;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.entity.res.ProjectStatsRes;
import jh.exp.project.core.mapper.ProjectMapper;
import jh.exp.project.service.service.bus.ProjectInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectInternalServiceImpl implements ProjectInternalService {
    private final ProjectMapper projectMapper;
    private final PersonService personService;

    @Override
    public SimplePageRes<Project> list(SimplePageReq<Object> req) {
        Page<Project> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        applyListFilter(wrapper, req.getQueryParam());
        wrapper.orderByDesc(Project::getProjectId);
        Page<Project> result = projectMapper.selectPage(page, wrapper);
        fillManagerName(result.getRecords());
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public Project detail(Long projectId) {
        Project entity = projectMapper.selectById(projectId);
        if (entity == null) {
            throw new RuntimeException("项目不存在");
        }
        fillManagerName(Collections.singletonList(entity));
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

    @Override
    public ProjectStatsRes stats() {
        List<Project> allProjects = projectMapper.selectList(new LambdaQueryWrapper<>());
        ProjectStatsRes res = new ProjectStatsRes();
        res.setTotalProjects((long) allProjects.size());
        res.setOngoingProjects(allProjects.stream()
                .filter(p -> "ONGOING".equalsIgnoreCase(p.getProjectStatus()))
                .count());
        res.setCompletedProjects(allProjects.stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getProjectStatus()))
                .count());
        res.setDelayedProjects(allProjects.stream()
                .filter(this::isDelayedProject)
                .count());
        BigDecimal totalBudget = allProjects.stream()
                .map(Project::getBudgetAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        res.setTotalBudget(totalBudget);
        // 当前项目域暂无统一沉淀的项目总成本字段，先返回0，后续可按财务口径扩展。
        res.setTotalCost(BigDecimal.ZERO);
        return res;
    }

    private void applyListFilter(LambdaQueryWrapper<Project> wrapper, Object rawQueryParam) {
        if (!(rawQueryParam instanceof Map<?, ?> queryMap)) {
            return;
        }
        String projectCode = toStr(queryMap.get("projectCode"));
        String projectName = toStr(queryMap.get("projectName"));
        String projectType = toStr(queryMap.get("projectType"));
        String projectStatus = toStr(queryMap.get("projectStatus"));
        String managerName = toStr(queryMap.get("managerName"));
        LocalDate startDateFrom = parseDate(queryMap.get("startDateFrom"));
        LocalDate startDateTo = parseDate(queryMap.get("startDateTo"));
        LocalDate planEndDateFrom = parseDate(queryMap.get("planEndDateFrom"));
        LocalDate planEndDateTo = parseDate(queryMap.get("planEndDateTo"));
        if (StringUtils.hasText(projectCode)) {
            wrapper.like(Project::getProjectCode, projectCode.trim());
        }
        if (StringUtils.hasText(projectName)) {
            wrapper.like(Project::getProjectName, projectName.trim());
        }
        if (StringUtils.hasText(projectType)) {
            wrapper.eq(Project::getProjectType, projectType.trim());
        }
        if (StringUtils.hasText(projectStatus)) {
            wrapper.eq(Project::getProjectStatus, projectStatus.trim());
        }
        if (startDateFrom != null) {
            wrapper.ge(Project::getStartDate, startDateFrom);
        }
        if (startDateTo != null) {
            wrapper.le(Project::getStartDate, startDateTo);
        }
        if (planEndDateFrom != null) {
            wrapper.ge(Project::getPlanEndDate, planEndDateFrom);
        }
        if (planEndDateTo != null) {
            wrapper.le(Project::getPlanEndDate, planEndDateTo);
        }
        if (StringUtils.hasText(managerName)) {
            List<Long> managerIds = queryPersonIdsByName(managerName.trim());
            // 人员服务异常时降级：不加负责人筛选，避免误返回空列表。
            if (managerIds == null) {
                return;
            }
            if (managerIds.isEmpty()) {
                wrapper.isNull(Project::getProjectId);
            } else {
                wrapper.in(Project::getManagerPersonId, managerIds);
            }
        }
    }

    private List<Long> queryPersonIdsByName(String managerName) {
        try {
            QueryPersonReq personReq = new QueryPersonReq();
            personReq.setPersonName(managerName);
            SimplePageReq<QueryPersonReq> req = new SimplePageReq<>();
            req.setPageNum(1);
            req.setPageSize(2000);
            req.setQueryParam(personReq);
            ApiResponse<SimplePageRes<PersonInfoRes>> resp = personService.queryPersonInfo(req);
            SimplePageRes<PersonInfoRes> pageRes = (resp != null && resp.isSuccess()) ? resp.getData() : null;
            if (pageRes == null || pageRes.getList() == null || pageRes.getList().isEmpty()) {
                return Collections.emptyList();
            }
            return pageRes.getList().stream()
                    .map(PersonInfoRes::getPersonId)
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("根据负责人姓名查询人员失败, managerName={}", managerName, ex);
            return null;
        }
    }

    private void fillManagerName(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return;
        }
        Set<Long> managerIds = projects.stream()
                .map(Project::getManagerPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (managerIds.isEmpty()) {
            return;
        }
        try {
            Map<Long, PersonDetailRes> personMap = personService.batchGetPersonByIds(new ArrayList<>(managerIds));
            if (personMap == null || personMap.isEmpty()) {
                return;
            }
            projects.forEach(project -> {
                PersonDetailRes person = personMap.get(project.getManagerPersonId());
                project.setManagerName(person == null ? null : person.getPersonName());
            });
        } catch (Exception ex) {
            log.error("批量查询项目负责人失败, managerIds={}", managerIds, ex);
        }
    }

    private boolean isDelayedProject(Project project) {
        if (project == null || project.getPlanEndDate() == null) {
            return false;
        }
        if ("COMPLETED".equalsIgnoreCase(project.getProjectStatus())) {
            return false;
        }
        return LocalDate.now().isAfter(project.getPlanEndDate());
    }

    private String toStr(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    private LocalDate parseDate(Object rawDate) {
        if (rawDate == null) {
            return null;
        }
        String text = String.valueOf(rawDate).trim();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception ex) {
            log.warn("项目列表筛选日期格式非法, value={}", text);
            return null;
        }
    }

}
