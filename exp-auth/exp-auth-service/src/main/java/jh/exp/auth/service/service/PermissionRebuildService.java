package jh.exp.auth.service.service;

import jh.exp.auth.core.mapper.AccountMapper;
import jh.exp.auth.core.mapper.PermissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限重建服务：根据角色/权限/组织/岗位变化找出受影响用户，批量换算账号，发起批量或懒重建。
 * <p>
 * 设计方案：权限快照 + 版本号 + 缓存 + 批量/懒重建。
 * 变更时使快照失效，下次请求时 cache miss 触发懒重建。
 */
@Service
public class PermissionRebuildService {

    private static final Logger log = LoggerFactory.getLogger(PermissionRebuildService.class);

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private PermissionSnapshotService permissionSnapshotService;
    @Autowired
    private UserPermissionProfileService userPermissionProfileService;

    /**
     * 角色变更（角色信息变更、角色删除等）时调用：使受影响用户快照失效。
     */
    public void onRoleChanged(Long roleId) {
        if (roleId == null) return;
        List<Long> accountIds = accountMapper.selectAccountIdsByRoleId(roleId);
        invalidateAndLog(accountIds, "roleChanged", roleId);
    }

    /**
     * 角色权限变更（角色-权限关联变更）时调用：使受影响用户快照失效。
     */
    public void onRolePermissionChanged(Long roleId) {
        if (roleId == null) return;
        List<Long> accountIds = accountMapper.selectAccountIdsByRoleId(roleId);
        invalidateAndLog(accountIds, "rolePermissionChanged", roleId);
    }

    /**
     * 角色授权变更（角色-主体关联变更）时调用：使受影响用户快照失效。
     *
     * @param principalType ACCOUNT/PERSON/POST/ORG
     * @param principalIds  主体ID列表
     */
    public void onRoleAssignChanged(Long roleId, String principalType, List<Long> principalIds) {
        if (roleId == null || CollectionUtils.isEmpty(principalIds)) return;
        List<Long> accountIds = resolveAccountIds(principalType, principalIds);
        invalidateAndLog(accountIds, "roleAssignChanged", roleId);
    }

    /**
     * 权限定义变更（菜单/权限编码变更）时调用：使使用该权限的所有角色受影响用户快照失效。
     */
    public void onPermCodeChanged(String permCode) {
        if (permCode == null || permCode.isBlank()) return;
        List<Long> roleIds = permissionMapper.selectRoleIdsByPermCode(permCode);
        if (roleIds == null || roleIds.isEmpty()) return;
        Set<Long> allAccountIds = new HashSet<>();
        for (Long roleId : roleIds) {
            List<Long> ids = accountMapper.selectAccountIdsByRoleId(roleId);
            if (ids != null) allAccountIds.addAll(ids);
        }
        invalidateAndLog(new ArrayList<>(allAccountIds), "permCodeChanged", permCode);
    }

    /**
     * 组织/岗位变更时调用（由跨服务通知触发）：使关联该组织/岗位的账号快照失效。
     *
     * @param principalType POST 或 ORG
     * @param principalIds  岗位ID或组织ID列表
     */
    public void onOrgOrPostChanged(String principalType, List<Long> principalIds) {
        if (principalType == null || CollectionUtils.isEmpty(principalIds)) return;
        if (!"POST".equals(principalType) && !"ORG".equals(principalType)) return;
        List<Long> accountIds = resolveAccountIds(principalType, principalIds);
        invalidateAndLog(accountIds, "orgOrPostChanged", principalType);
    }

    /**
     * 人员组织变更时调用（由跨服务通知触发）：使该人员关联的账号快照失效。
     */
    public void onPersonOrgChanged(List<Long> personIds) {
        if (CollectionUtils.isEmpty(personIds)) return;
        List<Long> accountIds = resolveAccountIds("PERSON", personIds);
        invalidateAndLog(accountIds, "personOrgChanged", null);
    }

    /**
     * 重建单个用户快照：先失效再构建（懒重建时由 buildFullSnapshot 自动完成，此处用于主动触发）。
     */
    public void rebuild(Long accountId) {
        if (accountId == null) return;
        permissionSnapshotService.invalidate(accountId);
        userPermissionProfileService.buildFullSnapshot(accountId);
        log.debug("权限快照已重建，accountId={}", accountId);
    }

    /**
     * 使指定账号列表的快照失效（角色授权变更时由 RoleAssignService 调用）。
     */
    public void invalidateByAccountIds(List<Long> accountIds) {
        if (CollectionUtils.isEmpty(accountIds)) return;
        permissionSnapshotService.invalidateBatch(accountIds);
        log.info("权限快照已失效，accountCount={}", accountIds.size());
    }

    /**
     * 批量重建：先批量失效，再逐个构建并写入缓存。
     */
    public void rebuildBatch(List<Long> accountIds) {
        if (CollectionUtils.isEmpty(accountIds)) return;
        permissionSnapshotService.invalidateBatch(accountIds);
        for (Long accountId : accountIds) {
            try {
                userPermissionProfileService.buildFullSnapshot(accountId);
            } catch (Exception e) {
                log.warn("批量重建时单用户失败，accountId={}", accountId, e);
            }
        }
        log.info("批量权限快照重建完成，count={}", accountIds.size());
    }

    /**
     * 根据主体类型和主体ID换算为账号ID列表。
     */
    private List<Long> resolveAccountIds(String principalType, List<Long> principalIds) {
        if (principalType == null || CollectionUtils.isEmpty(principalIds)) return List.of();
        if ("ACCOUNT".equals(principalType)) {
            return new ArrayList<>(principalIds);
        }
        return accountMapper.selectAccountIdsByPrincipals(principalType, principalIds);
    }

    private void invalidateAndLog(List<Long> accountIds, String reason, Object ref) {
        if (CollectionUtils.isEmpty(accountIds)) return;
        permissionSnapshotService.invalidateBatch(accountIds);
        log.info("权限快照已失效，reason={}, ref={}, count={}", reason, ref, accountIds.size());
    }
}
