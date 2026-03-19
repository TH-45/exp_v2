package jh.exp.auth.service.service;

import jh.exp.auth.core.entity.Menu;
import jh.exp.auth.core.entity.exp.PermissionExp;
import jh.exp.auth.core.mapper.AccountMapper;
import jh.exp.auth.core.mapper.MenuMapper;
import jh.exp.auth.core.mapper.PermissionMapper;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.auth.dto.PermissionProfileResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限画像服务：按 userId 组装 full snapshot。
 * <p>
 * 职责：查账号、查角色、批量查权限、分离 MENU/FUNC、归并 menuLevelMap 与 funcPermissionSet、
 * 生成 menuTree、生成 dataScopeSummary。
 */
@Service
public class UserPermissionProfileService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private PermissionSnapshotService permissionSnapshotService;

    /**
     * 构建用户完整权限画像（full snapshot），含 menuTree，供前端使用。
     * 优先从 Redis 缓存读取，未命中则计算并写入缓存。
     */
    public PermissionProfileResult buildFullSnapshot(Long accountId) {
        PermissionProfileResult cached = permissionSnapshotService.getFromCache(accountId);
        if (cached != null) {
            return cached;
        }
        PermissionProfileResult result = buildFullSnapshotInternal(accountId);
        if (result != null) {
            permissionSnapshotService.putToCache(result);
        }
        return result;
    }

    /**
     * 构建轻量权限画像（lite snapshot），不含 menuTree，供网关及下游服务透传头信息使用。
     * 优先从缓存读取 full 后剥离 menuTree，减少网络传输与序列化开销。
     */
    public PermissionProfileResult buildLiteSnapshot(Long accountId) {
        PermissionProfileResult full = buildFullSnapshot(accountId);
        if (full == null) return null;
        PermissionProfileResult lite = new PermissionProfileResult();
        lite.setUserId(full.getUserId());
        lite.setUsername(full.getUsername());
        lite.setRoles(full.getRoles());
        lite.setPermissionVersion(full.getPermissionVersion());
        lite.setMenuLevelMap(full.getMenuLevelMap());
        lite.setFuncPermissionSet(full.getFuncPermissionSet());
        lite.setDataScopeSummary(full.getDataScopeSummary());
        return lite;
    }

    private PermissionProfileResult buildFullSnapshotInternal(Long accountId) {
        var account = accountMapper.selectById(accountId);
        if (account == null) {
            return null;
        }

        // 1. 查账号拥有的所有角色（ACCOUNT/PERSON/POST/ORG 四类主体）
        var roleList = accountMapper.selectRolesForAccount(accountId);
        if (roleList == null) {
            roleList = Collections.emptyList();
        }
        if (roleList.isEmpty()) {
            return buildMinimalProfile(account, roleList, Collections.emptyMap(), Collections.emptySet(), null);
        }

        List<Long> roleIds = roleList.stream()
                .map(jh.exp.auth.core.entity.res.AccountRoleRes::getRoleId)
                .distinct()
                .toList();

        // 2. 批量查角色权限
        List<PermissionExp> perms = permissionMapper.selectPermissionsByRoleIds(roleIds);
        if (perms == null) {
            perms = Collections.emptyList();
        }

        // 3. 分离 MENU 与 FUNC，归并
        Map<String, Integer> menuLevelMap = new HashMap<>();
        Set<String> funcPermissionSet = new HashSet<>();

        for (PermissionExp p : perms) {
            if (!"ENABLED".equalsIgnoreCase(p.getStatus())) {
                continue;
            }
            String type = p.getPermType();
            if (type == null) {
                continue;
            }
            if ("MENU".equalsIgnoreCase(type)) {
                int level = parseGrantType(p.getGrantType());
                if (level > 0 && StringUtils.hasText(p.getPermCode())) {
                    menuLevelMap.merge(p.getPermCode(), level, Math::max);
                }
            } else if ("FUNC".equalsIgnoreCase(type)) {
                if (StringUtils.hasText(p.getPermCode())) {
                    funcPermissionSet.add(p.getPermCode());
                }
            }
        }

        // 4. 生成 menuTree
        List<PermissionProfileResult.MenuNode> menuTree = buildMenuTree(menuLevelMap);

        // 5. 生成 dataScopeSummary（简化：取角色中第一个的 data_scope，后续可扩展合并规则）
        CurrentUser.DataScopeSummary dataScopeSummary = buildDataScopeSummary(roleList);

        PermissionProfileResult result = buildMinimalProfile(account, roleList, menuLevelMap, funcPermissionSet, dataScopeSummary);
        result.setMenuTree(menuTree);
        return result;
    }

    /** 使指定用户快照失效（权限变更时调用） */
    public void invalidateSnapshot(Long accountId) {
        permissionSnapshotService.invalidate(accountId);
    }

    private PermissionProfileResult buildMinimalProfile(
            jh.exp.auth.core.entity.Account account,
            List<jh.exp.auth.core.entity.res.AccountRoleRes> roleList,
            Map<String, Integer> menuLevelMap,
            Set<String> funcPermissionSet,
            CurrentUser.DataScopeSummary dataScopeSummary) {
        List<String> roles = roleList == null ? Collections.emptyList()
                : roleList.stream()
                .map(jh.exp.auth.core.entity.res.AccountRoleRes::getRoleCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        PermissionProfileResult result = new PermissionProfileResult();
        result.setUserId(account.getAccountId());
        result.setUsername(account.getAccountDisplay() != null ? account.getAccountDisplay() : account.getAccountName());
        result.setRoles(roles);
        result.setPermissionVersion(System.currentTimeMillis());
        result.setMenuLevelMap(menuLevelMap);
        result.setFuncPermissionSet(new ArrayList<>(funcPermissionSet));
        result.setDataScopeSummary(dataScopeSummary);
        return result;
    }

    private int parseGrantType(String grantType) {
        if (!StringUtils.hasText(grantType)) {
            return 0;
        }
        try {
            int v = Integer.parseInt(grantType.trim());
            return (v >= 1 && v <= 3) ? v : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<PermissionProfileResult.MenuNode> buildMenuTree(Map<String, Integer> menuLevelMap) {
        List<Menu> allMenus = menuMapper.selectAllEnabledMenus();
        if (allMenus == null || allMenus.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Menu> menuById = allMenus.stream().collect(Collectors.toMap(Menu::getMenuId, m -> m));
        Map<Long, List<Menu>> childrenByParent = allMenus.stream()
                .filter(m -> m.getParentMenuId() != null && m.getParentMenuId() > 0)
                .collect(Collectors.groupingBy(Menu::getParentMenuId));

        Set<String> allowedCodes = menuLevelMap != null ? menuLevelMap.keySet() : Collections.emptySet();

        List<Menu> roots = allMenus.stream()
                .filter(m -> m.getParentMenuId() == null || m.getParentMenuId() == 0)
                .sorted(Comparator.comparing(Menu::getSortNo, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<PermissionProfileResult.MenuNode> result = new ArrayList<>();
        for (Menu root : roots) {
            PermissionProfileResult.MenuNode node = buildNode(root, childrenByParent, allowedCodes, menuLevelMap);
            if (node != null) {
                result.add(node);
            }
        }
        return result;
    }

    private PermissionProfileResult.MenuNode buildNode(Menu menu,
                                                       Map<Long, List<Menu>> childrenByParent,
                                                       Set<String> allowedCodes,
                                                       Map<String, Integer> menuLevelMap) {
        String nodeType = mapMenuType(menu.getMenuType());
        boolean isCatalog = "DIR".equalsIgnoreCase(menu.getMenuType()) || "CATALOG".equalsIgnoreCase(nodeType);

        List<Menu> children = childrenByParent.getOrDefault(menu.getMenuId(), Collections.emptyList());
        List<PermissionProfileResult.MenuNode> childNodes = new ArrayList<>();
        for (Menu child : children.stream().sorted(Comparator.comparing(Menu::getSortNo, Comparator.nullsLast(Comparator.naturalOrder()))).toList()) {
            PermissionProfileResult.MenuNode cn = buildNode(child, childrenByParent, allowedCodes, menuLevelMap);
            if (cn != null) {
                childNodes.add(cn);
            }
        }

        if (isCatalog) {
            if (childNodes.isEmpty()) {
                return null;
            }
            PermissionProfileResult.MenuNode node = new PermissionProfileResult.MenuNode();
            node.setMenuCode(menu.getMenuCode());
            node.setMenuName(menu.getMenuName());
            node.setIcon(menu.getIcon());
            node.setSortNo(menu.getSortNo());
            node.setPermLevel(null);
            node.setNodeType("CATALOG");
            node.setChildren(childNodes);
            return node;
        }

        if (!allowedCodes.contains(menu.getMenuCode())) {
            return null;
        }

        PermissionProfileResult.MenuNode node = new PermissionProfileResult.MenuNode();
        node.setMenuCode(menu.getMenuCode());
        node.setMenuName(menu.getMenuName());
        node.setIcon(menu.getIcon());
        node.setSortNo(menu.getSortNo());
        node.setPermLevel(menuLevelMap != null ? menuLevelMap.get(menu.getMenuCode()) : null);
        node.setNodeType("PAGE");
        node.setChildren(childNodes.isEmpty() ? Collections.emptyList() : childNodes);
        return node;
    }

    private String mapMenuType(String menuType) {
        if (menuType == null) return "PAGE";
        if ("DIR".equalsIgnoreCase(menuType)) return "CATALOG";
        return "PAGE";
    }

    private CurrentUser.DataScopeSummary buildDataScopeSummary(List<jh.exp.auth.core.entity.res.AccountRoleRes> roleList) {
        if (roleList == null || roleList.isEmpty()) {
            return null;
        }
        CurrentUser.DataScopeSummary summary = new CurrentUser.DataScopeSummary();
        summary.setScopeType("SELF");
        summary.setOrgIds(Collections.emptyList());
        summary.setProjectIds(Collections.emptyList());
        return summary;
    }
}
