package jh.exp.auth.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.auth.core.entity.*;
import jh.exp.auth.core.entity.req.RoleAssignSaveReq;
import jh.exp.auth.core.entity.res.RoleAssignRes;
import jh.exp.auth.core.mapper.*;
import jh.exp.auth.service.service.PermissionRebuildService;
import jh.exp.auth.service.service.bus.RoleAssignService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色授权服务实现：查询、保存角色授权，变更时触发权限重建。
 */
@Service
@RequiredArgsConstructor
public class RoleAssignServiceImpl implements RoleAssignService {

    private static final String STATUS_ENABLED = "ENABLED";
    private static final String TYPE_ACCOUNT = "ACCOUNT";
    private static final String TYPE_PERSON = "PERSON";
    private static final String TYPE_POST = "POST";
    private static final String TYPE_ORG = "ORG";

    private final RoleAssignMapper roleAssignMapper;
    private final AccountMapper accountMapper;
    private final PersonMapper personMapper;
    private final PositionMapper positionMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final PermissionRebuildService permissionRebuildService;

    @Override
    public RoleAssignRes listByRoleId(Long roleId) {
        if (roleId == null) {
            throw new BizException("角色ID不能为空");
        }
        List<RoleAssign> assigns = roleAssignMapper.selectList(
                new LambdaQueryWrapper<RoleAssign>()
                        .eq(RoleAssign::getRoleId, roleId)
                        .orderByAsc(RoleAssign::getPrincipalType, RoleAssign::getPrincipalId)
        );
        if (assigns == null || assigns.isEmpty()) {
            RoleAssignRes res = new RoleAssignRes();
            res.setRoleId(roleId);
            res.setGroups(buildEmptyGroups());
            return res;
        }

        Map<String, List<RoleAssign>> byType = assigns.stream()
                .collect(Collectors.groupingBy(RoleAssign::getPrincipalType));

        RoleAssignRes res = new RoleAssignRes();
        res.setRoleId(roleId);
        List<RoleAssignRes.PrincipalGroup> groups = new ArrayList<>();

        for (String type : List.of(TYPE_ACCOUNT, TYPE_PERSON, TYPE_POST, TYPE_ORG)) {
            List<RoleAssign> list = byType.get(type);
            if (list == null || list.isEmpty()) continue;
            RoleAssignRes.PrincipalGroup group = buildGroup(type, list);
            groups.add(group);
        }
        res.setGroups(groups);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(RoleAssignSaveReq req) {
        if (req == null || req.getRoleId() == null) {
            throw new BizException("角色ID不能为空");
        }

        Long roleId = req.getRoleId();

        // 1. 查询变更前的授权（用于后续收集受影响账号）
        List<RoleAssign> oldAssigns = roleAssignMapper.selectList(
                new LambdaQueryWrapper<RoleAssign>().eq(RoleAssign::getRoleId, roleId));

        // 2. 删除该角色下所有授权
        roleAssignMapper.delete(new LambdaQueryWrapper<RoleAssign>().eq(RoleAssign::getRoleId, roleId));

        // 3. 插入新授权
        Long currentUserId = null;
        CurrentUser cu = CurrentUserHolder.get();
        if (cu != null) {
            currentUserId = cu.getUserId();
        }

        List<RoleAssign> toInsert = new ArrayList<>();
        addAssigns(toInsert, roleId, TYPE_ACCOUNT, req.getAccountIds(), currentUserId);
        addAssigns(toInsert, roleId, TYPE_PERSON, req.getPersonIds(), currentUserId);
        addAssigns(toInsert, roleId, TYPE_POST, req.getPostIds(), currentUserId);
        addAssigns(toInsert, roleId, TYPE_ORG, req.getOrgIds(), currentUserId);

        for (RoleAssign a : toInsert) {
            roleAssignMapper.insert(a);
        }

        // 4. 收集受影响账号ID（变更前+变更后），使快照失效
        Set<Long> affectedAccountIds = new HashSet<>();
        for (RoleAssign a : oldAssigns) {
            if (TYPE_ACCOUNT.equals(a.getPrincipalType())) {
                affectedAccountIds.add(a.getPrincipalId());
            } else {
                List<Long> ids = accountMapper.selectAccountIdsByPrincipals(a.getPrincipalType(), List.of(a.getPrincipalId()));
                if (ids != null) affectedAccountIds.addAll(ids);
            }
        }
        if (!CollectionUtils.isEmpty(req.getAccountIds())) {
            affectedAccountIds.addAll(req.getAccountIds());
        }
        if (!CollectionUtils.isEmpty(req.getPersonIds())) {
            List<Long> ids = accountMapper.selectAccountIdsByPrincipals(TYPE_PERSON, req.getPersonIds());
            if (ids != null) affectedAccountIds.addAll(ids);
        }
        if (!CollectionUtils.isEmpty(req.getPostIds())) {
            List<Long> ids = accountMapper.selectAccountIdsByPrincipals(TYPE_POST, req.getPostIds());
            if (ids != null) affectedAccountIds.addAll(ids);
        }
        if (!CollectionUtils.isEmpty(req.getOrgIds())) {
            List<Long> ids = accountMapper.selectAccountIdsByPrincipals(TYPE_ORG, req.getOrgIds());
            if (ids != null) affectedAccountIds.addAll(ids);
        }
        if (!affectedAccountIds.isEmpty()) {
            permissionRebuildService.invalidateByAccountIds(new ArrayList<>(affectedAccountIds));
        }
    }

    private void addAssigns(List<RoleAssign> list, Long roleId, String type, List<Long> ids, Long createdBy) {
        if (ids == null || ids.isEmpty()) return;
        for (Long pid : ids) {
            RoleAssign a = new RoleAssign();
            a.setRoleId(roleId);
            a.setPrincipalType(type);
            a.setPrincipalId(pid);
            a.setStatus(STATUS_ENABLED);
            a.setCreatedBy(createdBy);
            a.setCreatedTime(LocalDateTime.now());
            list.add(a);
        }
    }

    private RoleAssignRes.PrincipalGroup buildGroup(String type, List<RoleAssign> list) {
        String typeName = switch (type) {
            case TYPE_ACCOUNT -> "账号";
            case TYPE_PERSON -> "人员";
            case TYPE_POST -> "岗位";
            case TYPE_ORG -> "组织";
            default -> type;
        };
        RoleAssignRes.PrincipalGroup group = new RoleAssignRes.PrincipalGroup();
        group.setPrincipalType(type);
        group.setPrincipalTypeName(typeName);
        List<Long> principalIds = list.stream().map(RoleAssign::getPrincipalId).distinct().toList();
        Map<Long, String> codeMap = new HashMap<>();
        Map<Long, String> nameMap = new HashMap<>();
        // selectByIds 不支持空集合，需先判断
        if (principalIds.isEmpty()) {
            group.setItems(list.stream().map(a -> {
                RoleAssignRes.PrincipalItem item = new RoleAssignRes.PrincipalItem();
                item.setId(a.getId());
                item.setPrincipalId(a.getPrincipalId());
                item.setPrincipalCode(null);
                item.setPrincipalName(null);
                item.setStatus(a.getStatus());
                item.setStartTime(a.getStartTime());
                item.setEndTime(a.getEndTime());
                return item;
            }).toList());
            return group;
        }
        switch (type) {
            case TYPE_ACCOUNT -> {
                List<Account> accounts = accountMapper.selectByIds(principalIds);
                if (accounts != null) {
                    for (Account a : accounts) {
                        nameMap.put(a.getAccountId(), a.getAccountDisplay() != null ? a.getAccountDisplay() : a.getAccountName());
                        codeMap.put(a.getAccountId(), a.getAccountName());
                    }
                }
            }
            case TYPE_PERSON -> {
                List<Person> persons = personMapper.selectByIds(principalIds);
                if (persons != null) {
                    for (Person p : persons) {
                        codeMap.put(p.getPersonId(), p.getPersonCode());
                        nameMap.put(p.getPersonId(), p.getPersonName());
                    }
                }
            }
            case TYPE_POST -> {
                List<Position> positions = positionMapper.selectByIds(principalIds);
                if (positions != null) {
                    for (Position p : positions) {
                        codeMap.put(p.getPostId(), p.getPostCode());
                        nameMap.put(p.getPostId(), p.getPostName());
                    }
                }
            }
            case TYPE_ORG -> {
                List<OrgUnit> orgs = orgUnitMapper.selectByIds(principalIds);
                if (orgs != null) {
                    for (OrgUnit o : orgs) {
                        codeMap.put(o.getOrgId(), o.getOrgCode());
                        nameMap.put(o.getOrgId(), o.getOrgName());
                    }
                }
            }
            default -> {
            }
        }
        List<RoleAssignRes.PrincipalItem> items = new ArrayList<>();
        for (RoleAssign a : list) {
            RoleAssignRes.PrincipalItem item = new RoleAssignRes.PrincipalItem();
            item.setId(a.getId());
            item.setPrincipalId(a.getPrincipalId());
            item.setPrincipalCode(codeMap.get(a.getPrincipalId()));
            item.setPrincipalName(nameMap.get(a.getPrincipalId()));
            item.setStatus(a.getStatus());
            item.setStartTime(a.getStartTime());
            item.setEndTime(a.getEndTime());
            items.add(item);
        }
        group.setItems(items);
        return group;
    }

    private List<RoleAssignRes.PrincipalGroup> buildEmptyGroups() {
        return List.of();
    }
}