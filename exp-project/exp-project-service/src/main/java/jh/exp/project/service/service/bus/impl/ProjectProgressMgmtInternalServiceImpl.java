package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.ProjectSchedule;
import jh.exp.project.core.entity.ProjectScheduleLog;
import jh.exp.project.core.entity.req.ProjectMilestoneCreateReq;
import jh.exp.project.core.entity.req.ProjectMilestoneDeleteReq;
import jh.exp.project.core.entity.req.ProjectMilestoneProgressUpdateReq;
import jh.exp.project.core.entity.req.ProjectMilestoneUpdateReq;
import jh.exp.project.core.entity.res.ProjectMilestoneRes;
import jh.exp.project.core.entity.res.ProjectProgressRes;
import jh.exp.project.core.mapper.ProjectMapper;
import jh.exp.project.core.mapper.ProjectScheduleLogMapper;
import jh.exp.project.core.mapper.ProjectScheduleMapper;
import jh.exp.project.service.service.bus.ProjectProgressMgmtInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectProgressMgmtInternalServiceImpl implements ProjectProgressMgmtInternalService {
    private static final String STATUS_NOT_START = "NOT_START";
    private static final String STATUS_ONGOING = "ONGOING";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String STATUS_DELAYED = "DELAYED";

    private static final String UI_STATUS_NOT_START = "NOT_STARTED";
    private static final String UI_STATUS_ONGOING = "ONGOING";
    private static final String UI_STATUS_COMPLETED = "COMPLETED";
    private static final String UI_STATUS_DELAYED = "DELAYED";

    private static final String CHANGE_TYPE_PROGRESS_UPDATE = "PROGRESS_UPDATE";

    private final ProjectScheduleMapper projectScheduleMapper;
    private final ProjectScheduleLogMapper projectScheduleLogMapper;
    private final ProjectMapper projectMapper;
    private final PersonService personService;

    @Override
    public ProjectProgressRes detail(Long projectId) {
        checkProjectExists(projectId);
        List<ProjectSchedule> milestones = listByProject(projectId, true);
        List<ProjectSchedule> allNodes = listByProject(projectId, false);

        ProjectProgressRes res = new ProjectProgressRes();
        res.setProjectId(projectId);
        res.setMilestones(enrichMilestones(milestones));
        res.setTotalMilestones(milestones.size());
        res.setCompletedMilestones((int) milestones.stream().filter(s -> STATUS_FINISHED.equalsIgnoreCase(s.getStatus())).count());
        res.setDelayedMilestones((int) milestones.stream().filter(s -> STATUS_DELAYED.equalsIgnoreCase(s.getStatus())).count());
        res.setOverallProgress(calcOverallProgress(milestones, allNodes));
        return res;
    }

    @Override
    @Transactional
    public ProjectMilestoneRes createMilestone(ProjectMilestoneCreateReq req) {
        checkProjectExists(req.getProjectId());
        validatePlanDates(req.getPlannedStartDate(), req.getPlannedEndDate());
        validatePredecessor(req.getProjectId(), null, req.getPredecessorMilestoneId());

        ProjectSchedule row = new ProjectSchedule();
        row.setProjectId(req.getProjectId());
        row.setScheduleName(req.getName());
        row.setRemark(req.getDescription());
        row.setPlanStartDate(req.getPlannedStartDate());
        row.setPlanEndDate(req.getPlannedEndDate());
        row.setResponsibleUserId(req.getResponsiblePersonId());
        row.setPredecessorScheduleId(req.getPredecessorMilestoneId());
        row.setMilestoneFlag(1);
        row.setProgressPercent(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row.setStatus(STATUS_NOT_START);
        row.setSortNo(calcNextSortNo(req.getProjectId()));
        projectScheduleMapper.insert(row);
        return toRes(projectScheduleMapper.selectById(row.getScheduleId()), safeBatchPersons(Collections.singleton(row.getResponsibleUserId())));
    }

    @Override
    @Transactional
    public ProjectMilestoneRes updateMilestone(ProjectMilestoneUpdateReq req) {
        ProjectSchedule old = requireMilestone(req.getId());
        validatePlanDates(req.getPlannedStartDate(), req.getPlannedEndDate());
        validatePredecessor(old.getProjectId(), old.getScheduleId(), req.getPredecessorMilestoneId());

        old.setScheduleName(req.getName());
        old.setRemark(req.getDescription());
        old.setPlanStartDate(req.getPlannedStartDate());
        old.setPlanEndDate(req.getPlannedEndDate());
        old.setResponsibleUserId(req.getResponsiblePersonId());
        old.setPredecessorScheduleId(req.getPredecessorMilestoneId());
        projectScheduleMapper.updateById(old);
        return toRes(projectScheduleMapper.selectById(old.getScheduleId()), safeBatchPersons(Collections.singleton(old.getResponsibleUserId())));
    }

    @Override
    @Transactional
    public void deleteMilestone(ProjectMilestoneDeleteReq req) {
        ProjectSchedule old = requireMilestone(req.getId());
        LambdaQueryWrapper<ProjectSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectSchedule::getProjectId, old.getProjectId())
                .eq(ProjectSchedule::getPredecessorScheduleId, old.getScheduleId())
                .last("limit 1");
        ProjectSchedule dependent = projectScheduleMapper.selectOne(wrapper);
        if (dependent != null) {
            throw new RuntimeException("当前里程碑被其他里程碑依赖，禁止删除");
        }
        projectScheduleMapper.deleteById(old.getScheduleId());
    }

    @Override
    @Transactional
    public ProjectMilestoneRes updateProgress(ProjectMilestoneProgressUpdateReq req) {
        ProjectSchedule row = requireMilestone(req.getMilestoneId());
        validateProgressDates(req.getActualStartDate(), req.getActualEndDate());
        if (req.getProgress() == null) {
            throw new RuntimeException("progress不能为空");
        }
        BigDecimal progress = req.getProgress().setScale(2, RoundingMode.HALF_UP);
        BigDecimal before = row.getProgressPercent() == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : row.getProgressPercent().setScale(2, RoundingMode.HALF_UP);

        row.setActualStartDate(req.getActualStartDate());
        row.setActualEndDate(req.getActualEndDate());
        row.setProgressPercent(progress);
        row.setStatus(calcStatus(progress, row.getPlanEndDate(), req.getActualEndDate()));
        projectScheduleMapper.updateById(row);

        ProjectScheduleLog logRow = new ProjectScheduleLog();
        logRow.setScheduleId(row.getScheduleId());
        logRow.setProjectId(row.getProjectId());
        logRow.setChangeTime(LocalDateTime.now());
        logRow.setBeforeProgress(before);
        logRow.setAfterProgress(progress);
        logRow.setChangeType(CHANGE_TYPE_PROGRESS_UPDATE);
        logRow.setOperatorUserId(currentUserId());
        logRow.setRemark(req.getRemarks());
        projectScheduleLogMapper.insert(logRow);

        return toRes(projectScheduleMapper.selectById(row.getScheduleId()), safeBatchPersons(Collections.singleton(row.getResponsibleUserId())));
    }

    private void checkProjectExists(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
    }

    private ProjectSchedule requireMilestone(Long scheduleId) {
        ProjectSchedule row = projectScheduleMapper.selectById(scheduleId);
        if (row == null) {
            throw new RuntimeException("里程碑不存在");
        }
        if (row.getMilestoneFlag() == null || row.getMilestoneFlag() != 1) {
            throw new RuntimeException("当前节点不是里程碑节点");
        }
        return row;
    }

    private List<ProjectSchedule> listByProject(Long projectId, boolean onlyMilestone) {
        LambdaQueryWrapper<ProjectSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectSchedule::getProjectId, projectId);
        if (onlyMilestone) {
            wrapper.eq(ProjectSchedule::getMilestoneFlag, 1);
        }
        wrapper.orderByAsc(ProjectSchedule::getSortNo).orderByAsc(ProjectSchedule::getScheduleId);
        return projectScheduleMapper.selectList(wrapper);
    }

    private void validatePlanDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("计划开始和结束日期不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("计划结束日期不能早于计划开始日期");
        }
    }

    private void validateProgressDates(LocalDate actualStartDate, LocalDate actualEndDate) {
        if (actualStartDate != null && actualEndDate != null && actualEndDate.isBefore(actualStartDate)) {
            throw new RuntimeException("实际结束日期不能早于实际开始日期");
        }
    }

    private void validatePredecessor(Long projectId, Long currentId, Long predecessorId) {
        if (predecessorId == null) {
            return;
        }
        if (currentId != null && Objects.equals(currentId, predecessorId)) {
            throw new RuntimeException("前置里程碑不能选择自身");
        }

        ProjectSchedule predecessor = projectScheduleMapper.selectById(predecessorId);
        if (predecessor == null || !Objects.equals(predecessor.getProjectId(), projectId)) {
            throw new RuntimeException("前置里程碑不存在或不属于当前项目");
        }
        if (predecessor.getMilestoneFlag() == null || predecessor.getMilestoneFlag() != 1) {
            throw new RuntimeException("前置里程碑必须是里程碑节点");
        }

        if (currentId != null) {
            List<ProjectSchedule> milestones = listByProject(projectId, true);
            Map<Long, Long> predecessorMap = milestones.stream()
                    .collect(Collectors.toMap(ProjectSchedule::getScheduleId, ProjectSchedule::getPredecessorScheduleId, (a, b) -> a));
            predecessorMap.put(currentId, predecessorId);
            if (hasCycle(predecessorMap, currentId)) {
                throw new RuntimeException("前置里程碑配置形成循环依赖");
            }
        }
    }

    private boolean hasCycle(Map<Long, Long> predecessorMap, Long startId) {
        Set<Long> visited = new LinkedHashSet<>();
        Long cursor = startId;
        while (cursor != null) {
            if (!visited.add(cursor)) {
                return true;
            }
            cursor = predecessorMap.get(cursor);
        }
        return false;
    }

    private int calcNextSortNo(Long projectId) {
        LambdaQueryWrapper<ProjectSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectSchedule::getProjectId, projectId).orderByDesc(ProjectSchedule::getSortNo).last("limit 1");
        ProjectSchedule top = projectScheduleMapper.selectOne(wrapper);
        if (top == null || top.getSortNo() == null) {
            return 1;
        }
        return top.getSortNo() + 1;
    }

    private BigDecimal calcOverallProgress(List<ProjectSchedule> milestones, List<ProjectSchedule> allNodes) {
        List<ProjectSchedule> calcSource = milestones.isEmpty() ? allNodes : milestones;
        if (calcSource.isEmpty()) {
            return BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP);
        }
        BigDecimal sum = calcSource.stream()
                .map(ProjectSchedule::getProgressPercent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(calcSource.size()), 0, RoundingMode.HALF_UP);
    }

    private String calcStatus(BigDecimal progress, LocalDate planEndDate, LocalDate actualEndDate) {
        if (progress.compareTo(BigDecimal.valueOf(100)) >= 0) {
            if (planEndDate != null && actualEndDate != null && actualEndDate.isAfter(planEndDate)) {
                return STATUS_DELAYED;
            }
            return STATUS_FINISHED;
        }
        if (progress.compareTo(BigDecimal.ZERO) <= 0) {
            return STATUS_NOT_START;
        }
        if (planEndDate != null && LocalDate.now().isAfter(planEndDate)) {
            return STATUS_DELAYED;
        }
        return STATUS_ONGOING;
    }

    private Long currentUserId() {
        CurrentUser currentUser = CurrentUserHolder.get();
        return currentUser == null ? null : currentUser.getUserId();
    }

    private List<ProjectMilestoneRes> enrichMilestones(List<ProjectSchedule> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> personIds = rows.stream()
                .map(ProjectSchedule::getResponsibleUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, PersonDetailRes> personMap = safeBatchPersons(personIds);
        List<ProjectMilestoneRes> result = new ArrayList<>(rows.size());
        for (ProjectSchedule row : rows) {
            result.add(toRes(row, personMap));
        }
        return result;
    }

    private ProjectMilestoneRes toRes(ProjectSchedule row, Map<Long, PersonDetailRes> personMap) {
        ProjectMilestoneRes res = new ProjectMilestoneRes();
        res.setId(row.getScheduleId());
        res.setProjectId(row.getProjectId());
        res.setName(row.getScheduleName());
        res.setDescription(row.getRemark());
        res.setPlannedStartDate(row.getPlanStartDate());
        res.setPlannedEndDate(row.getPlanEndDate());
        res.setActualStartDate(row.getActualStartDate());
        res.setActualEndDate(row.getActualEndDate());
        res.setProgress(row.getProgressPercent() == null ? BigDecimal.ZERO : row.getProgressPercent());
        res.setStatus(toUiStatus(row.getStatus()));
        res.setPredecessorMilestoneId(row.getPredecessorScheduleId());
        res.setResponsiblePersonId(row.getResponsibleUserId());
        PersonDetailRes person = personMap.get(row.getResponsibleUserId());
        res.setResponsiblePerson(person == null ? null : person.getPersonName());
        return res;
    }

    private String toUiStatus(String dbStatus) {
        if (STATUS_FINISHED.equalsIgnoreCase(dbStatus)) {
            return UI_STATUS_COMPLETED;
        }
        if (STATUS_ONGOING.equalsIgnoreCase(dbStatus)) {
            return UI_STATUS_ONGOING;
        }
        if (STATUS_DELAYED.equalsIgnoreCase(dbStatus)) {
            return UI_STATUS_DELAYED;
        }
        return UI_STATUS_NOT_START;
    }

    private Map<Long, PersonDetailRes> safeBatchPersons(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, PersonDetailRes> map = personService.batchGetPersonByIds(new ArrayList<>(ids));
            return map == null ? Collections.emptyMap() : map;
        } catch (Exception ex) {
            log.error("批量查询里程碑负责人失败, ids={}", ids, ex);
            return Collections.emptyMap();
        }
    }
}
