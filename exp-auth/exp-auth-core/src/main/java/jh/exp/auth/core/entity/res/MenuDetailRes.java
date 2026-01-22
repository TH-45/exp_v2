package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 菜单详情响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuDetailRes {

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
     * 备注
     */
    private String remark;

}