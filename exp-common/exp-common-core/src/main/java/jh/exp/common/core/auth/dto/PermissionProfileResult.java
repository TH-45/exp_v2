package jh.exp.common.core.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 权限画像接口返回的完整快照（full snapshot），供前端使用。
 * <p>
 * 权限设计方案：登录后采用两层获取数据，第一层登录返回轻量数据，
 * 第二层通过本接口获取完整权限画像。
 */
@Data
@NoArgsConstructor
public class PermissionProfileResult implements Serializable {

    private Long userId;
    private String username;
    private List<String> roles;
    private Long permissionVersion;
    /** 菜单树，仅返回当前用户可见的节点 */
    private List<MenuNode> menuTree;
    /** 菜单权限等级映射：menuCode -> level (1=查看, 2=编辑, 3=管理) */
    private Map<String, Integer> menuLevelMap;
    /** 特殊权限编码集合 */
    private List<String> funcPermissionSet;
    /** 数据权限摘要 */
    private CurrentUser.DataScopeSummary dataScopeSummary;

    @Data
    @NoArgsConstructor
    public static class MenuNode implements Serializable {
        private String menuCode;
        private String menuName;
        private String icon;
        private Integer sortNo;
        private Integer permLevel;
        private String nodeType;
        private List<MenuNode> children;
    }
}
