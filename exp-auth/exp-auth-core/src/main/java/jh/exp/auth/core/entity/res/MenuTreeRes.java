package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单树响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuTreeRes {

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentMenuId;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型
     */
    private String menuType;

    /**
     * 前端路由
     */
    private String routePath;

    /**
     * 前端组件
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 是否显示
     */
    private Integer visible;

    /**
     * 状态
     */
    private String status;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 子菜单列表
     */
    private List<MenuTreeRes> children;

    /**
     * 是否有子节点
     */
    private Boolean hasChildren;

}