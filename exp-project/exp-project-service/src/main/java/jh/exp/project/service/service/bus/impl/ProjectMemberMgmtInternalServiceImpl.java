package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.auth.clinet.api.bus.OrgUnitService;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.clinet.api.bus.PositionService;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.entity.res.PositionDetailRes;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.ProjectStaffAssign;
import jh.exp.project.core.entity.req.ProjectMemberCreateReq;
import jh.exp.project.core.entity.req.ProjectMemberDeleteReq;
import jh.exp.project.core.entity.req.ProjectMemberUpdateReq;
import jh.exp.project.core.entity.res.ProjectMemberRes;
import jh.exp.project.core.mapper.ProjectMapper;
import jh.exp.project.core.mapper.ProjectStaffAssignMapper;
import jh.exp.project.service.service.bus.ProjectMemberMgmtInternalService;
import jh.exp.common.core.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ProjectMemberMgmtInternalServiceImpl implements ProjectMemberMgmtInternalService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DB_ONGOING = "ONGOING";
    private static final String STATUS_DB_FINISHED = "FINISHED";

    private final ProjectStaffAssignMapper projectStaffAssignMapper;
    private final ProjectMapper projectMapper;
    private final PersonService personService;
    private final OrgUnitService orgUnitService;
    private final PositionService positionService;

    @Override
    public List<ProjectMemberRes> listByProjectId(Long projectId) {
        checkProjectExists(projectId);
        LambdaQueryWrapper<ProjectStaffAssign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectStaffAssign::getProjectId, projectId)
                .orderByDesc(ProjectStaffAssign::getIsLeader)
                .orderByDesc(ProjectStaffAssign::getStartDate)
                .orderByDesc(ProjectStaffAssign::getId);
        List<ProjectStaffAssign> rows = projectStaffAssignMapper.selectList(wrapper);
        return enrich(rows);
    }

    @Override
    @Transactional
    public ProjectMemberRes create(ProjectMemberCreateReq req) {
        checkProjectExists(req.getProjectId());
        String dbStatus = toDbStatus(req.getStatus());
        validateDates(req.getJoinDate(), req.getLeaveDate(), dbStatus);
        if (STATUS_DB_ONGOING.equals(dbStatus)) {
            ensureNoDuplicateOngoingMember(req.getProjectId(), req.getUserId(), null);
        }

        ProjectStaffAssign entity = new ProjectStaffAssign();
        entity.setProjectId(req.getProjectId());
        entity.setPersonId(req.getUserId());
        entity.setOrgId(req.getOrgId());
        entity.setPostId(req.getPostId());
        entity.setProjectRoleCode(req.getProjectRoleCode());
        entity.setProjectRoleName(req.getProjectRoleName());
        entity.setIsLeader(Boolean.TRUE.equals(req.getIsManager()) ? 1 : 0);
        entity.setStartDate(req.getJoinDate());
        entity.setEndDate(req.getLeaveDate());
        entity.setStatus(dbStatus);
        entity.setRemark(req.getResponsibilities());

        projectStaffAssignMapper.insert(entity);
        return getById(entity.getId());
    }

    @Override
    @Transactional
    public ProjectMemberRes update(ProjectMemberUpdateReq req) {
        ProjectStaffAssign old = requireExisting(req.getId());
        Long targetUserId = req.getUserId() != null ? req.getUserId() : old.getPersonId();
        String targetStatus = req.getStatus() != null ? toDbStatus(req.getStatus()) : old.getStatus();
        validateDates(
                req.getJoinDate() != null ? req.getJoinDate() : old.getStartDate(),
                req.getLeaveDate() != null ? req.getLeaveDate() : old.getEndDate(),
                targetStatus
        );
        if (STATUS_DB_ONGOING.equals(targetStatus)) {
            ensureNoDuplicateOngoingMember(old.getProjectId(), targetUserId, old.getId());
        }

        if (req.getUserId() != null && !Objects.equals(req.getUserId(), old.getPersonId())) {
            old.setPersonId(req.getUserId());
        }
        if (req.getOrgId() != null) {
            old.setOrgId(req.getOrgId());
        }
        if (req.getPostId() != null) {
            old.setPostId(req.getPostId());
        }
        if (req.getProjectRoleCode() != null) {
            old.setProjectRoleCode(req.getProjectRoleCode());
        }
        if (req.getProjectRoleName() != null) {
            old.setProjectRoleName(req.getProjectRoleName());
        }
        if (req.getIsManager() != null) {
            old.setIsLeader(Boolean.TRUE.equals(req.getIsManager()) ? 1 : 0);
        }
        if (req.getJoinDate() != null) {
            old.setStartDate(req.getJoinDate());
        }
        if (req.getLeaveDate() != null) {
            old.setEndDate(req.getLeaveDate());
        } else if (req.getStatus() != null && STATUS_DB_ONGOING.equals(toDbStatus(req.getStatus()))) {
            // 复职时若未传离项日期，清空历史离项日期，保持状态与日期一致
            old.setEndDate(null);
        }
        if (req.getStatus() != null) {
            old.setStatus(toDbStatus(req.getStatus()));
        }
        if (req.getResponsibilities() != null) {
            old.setRemark(req.getResponsibilities());
        }

        projectStaffAssignMapper.updateById(old);
        return getById(old.getId());
    }

    @Override
    @Transactional
    public void delete(ProjectMemberDeleteReq req) {
        requireExisting(req.getId());
        projectStaffAssignMapper.deleteById(req.getId());
    }

    private ProjectMemberRes getById(Long id) {
        ProjectStaffAssign row = requireExisting(id);
        List<ProjectMemberRes> list = enrich(Collections.singletonList(row));
        return list.isEmpty() ? null : list.get(0);
    }

    private ProjectStaffAssign requireExisting(Long id) {
        ProjectStaffAssign row = projectStaffAssignMapper.selectById(id);
        if (row == null) {
            throw new RuntimeException("项目成员不存在");
        }
        return row;
    }

    private void checkProjectExists(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
    }

    private void ensureNoDuplicateOngoingMember(Long projectId, Long personId, Long excludeId) {
        LambdaQueryWrapper<ProjectStaffAssign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectStaffAssign::getProjectId, projectId)
                .eq(ProjectStaffAssign::getPersonId, personId)
                .eq(ProjectStaffAssign::getStatus, STATUS_DB_ONGOING);
        if (excludeId != null) {
            wrapper.ne(ProjectStaffAssign::getId, excludeId);
        }
        wrapper
                .last("limit 1");
        ProjectStaffAssign exists = projectStaffAssignMapper.selectOne(wrapper);
        if (exists != null) {
            throw new RuntimeException("该人员已在项目中");
        }
    }

    private void validateDates(java.time.LocalDate joinDate, java.time.LocalDate leaveDate, String dbStatus) {
        if (joinDate == null) {
            throw new RuntimeException("入项日期不能为空");
        }
        if (leaveDate != null && leaveDate.isBefore(joinDate)) {
            throw new RuntimeException("离项日期不能早于入项日期");
        }
        if (STATUS_DB_ONGOING.equals(dbStatus) && leaveDate != null) {
            throw new RuntimeException("在项状态不能填写离项日期");
        }
        if (STATUS_DB_FINISHED.equals(dbStatus) && leaveDate == null) {
            throw new RuntimeException("离职状态必须填写离项日期");
        }
    }

    private List<ProjectMemberRes> enrich(List<ProjectStaffAssign> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> personIds = rows.stream().map(ProjectStaffAssign::getPersonId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> orgIds = rows.stream().map(ProjectStaffAssign::getOrgId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> postIds = rows.stream().map(ProjectStaffAssign::getPostId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, PersonDetailRes> personMap = safeBatchPerson(personIds);
        Map<Long, OrgUnitDetailRes> orgMap = safeBatchOrg(orgIds);
        Map<Long, PositionDetailRes> postMap = safeBatchPost(postIds);

        List<ProjectMemberRes> result = new ArrayList<>(rows.size());
        for (ProjectStaffAssign row : rows) {
            ProjectMemberRes res = new ProjectMemberRes();
            res.setId(row.getId());
            res.setProjectId(row.getProjectId());
            res.setUserId(row.getPersonId());
            res.setOrgId(row.getOrgId());
            res.setPostId(row.getPostId());
            res.setProjectRoleCode(row.getProjectRoleCode());
            res.setProjectRoleName(row.getProjectRoleName());
            res.setIsManager(row.getIsLeader() != null && row.getIsLeader() == 1);
            res.setJoinDate(row.getStartDate());
            res.setLeaveDate(row.getEndDate());
            res.setStatus(toUiStatus(row.getStatus()));
            res.setResponsibilities(row.getRemark());

            PersonDetailRes person = personMap.get(row.getPersonId());
            if (person != null) {
                res.setUserName(person.getPersonName());
                if (res.getOrgId() == null) {
                    res.setOrgId(person.getOrgId());
                }
                if (res.getPostId() == null) {
                    res.setPostId(person.getPostId());
                }
                if (res.getDepartment() == null) {
                    res.setDepartment(person.getOrgName());
                }
                if (res.getPost() == null) {
                    res.setPost(person.getPostName());
                }
            }

            if (res.getDepartment() == null && res.getOrgId() != null) {
                OrgUnitDetailRes org = orgMap.get(res.getOrgId());
                if (org != null) {
                    res.setDepartment(org.getOrgName());
                }
            }
            if (res.getPost() == null && res.getPostId() != null) {
                PositionDetailRes post = postMap.get(res.getPostId());
                if (post != null) {
                    res.setPost(post.getPostName());
                }
            }
            result.add(res);
        }
        return result;
    }

    private Map<Long, PersonDetailRes> safeBatchPerson(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, PersonDetailRes> map = personService.batchGetPersonByIds(new ArrayList<>(ids));
            return map == null ? Collections.emptyMap() : map;
        } catch (Exception ex) {
            log.error("批量查询人员信息失败, ids={}", ids, ex);
            return Collections.emptyMap();
        }
    }

    private Map<Long, OrgUnitDetailRes> safeBatchOrg(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            ApiResponse<Map<Long, OrgUnitDetailRes>> resp = orgUnitService.batchGetOrgUnitByIds(new ArrayList<>(ids));
            Map<Long, OrgUnitDetailRes> map = (resp != null && resp.isSuccess()) ? resp.getData() : null;
            return map == null ? Collections.emptyMap() : map;
        } catch (Exception ex) {
            log.error("批量查询组织信息失败, ids={}", ids, ex);
            return Collections.emptyMap();
        }
    }

    private Map<Long, PositionDetailRes> safeBatchPost(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            ApiResponse<Map<Long, PositionDetailRes>> resp = positionService.batchGetPositionByIds(new ArrayList<>(ids));
            Map<Long, PositionDetailRes> map = (resp != null && resp.isSuccess()) ? resp.getData() : null;
            return map == null ? Collections.emptyMap() : map;
        } catch (Exception ex) {
            log.error("批量查询岗位信息失败, ids={}", ids, ex);
            return Collections.emptyMap();
        }
    }

    private String toDbStatus(String uiStatus) {
        if (uiStatus == null) {
            return STATUS_DB_ONGOING;
        }
        if (STATUS_ACTIVE.equalsIgnoreCase(uiStatus)) {
            return STATUS_DB_ONGOING;
        }
        return STATUS_DB_FINISHED;
    }

    private String toUiStatus(String dbStatus) {
        if (STATUS_DB_ONGOING.equalsIgnoreCase(dbStatus)) {
            return STATUS_ACTIVE;
        }
        return "INACTIVE";
    }
}
