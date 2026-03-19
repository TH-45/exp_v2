package jh.exp.common.core.constant;

public class ServiceContext {
    public static final String REQUEST_SOURCE_HEADER = "X-Request-Source";
    //X-User-Id 用户ID
    public static final String USER_ID_HEADER = "X-User-Id";
    //X-User-Name 用户名
    public static final String USER_NAME_HEADER = "X-User-Name";
    //X-Dept-Id 部门ID
    public static final String DEPT_ID_HEADER = "X-Dept-Id";
    //X-Dept-Name 部门名称
    public static final String DEPT_NAME_HEADER = "X-Dept-Name";
    //X-Roles 角色
    public static final String ROLES_HEADER = "X-Roles";
    //X-User-Roles 用户角色
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    //X-Permissions 权限
    public static final String PERMISSIONS_HEADER = "X-Permissions";
    //X-User-Permissions 用户权限
    public static final String USER_PERMISSIONS_HEADER = "X-User-Permissions";

    // ========== 权限设计方案新增头字段 ==========
    /** 权限版本号 */
    public static final String PERMISSION_VERSION_HEADER = "X-Permission-Version";
    /** 菜单权限等级映射 JSON */
    public static final String MENU_LEVEL_MAP_HEADER = "X-Menu-Level-Map";
    /** 特殊权限集合 JSON */
    public static final String FUNC_PERMISSIONS_HEADER = "X-Func-Permissions";
    /** 数据权限摘要 JSON */
    public static final String DATA_SCOPE_HEADER = "X-Data-Scope";
}
