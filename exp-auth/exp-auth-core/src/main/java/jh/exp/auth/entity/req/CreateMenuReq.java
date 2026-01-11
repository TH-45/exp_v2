package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建菜单请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMenuReq {

    /**
     * 父菜单ID（根菜单为0或null）
     */
    private Long parentMenuId;

    /**
     * 菜单编码（唯一）
     */
    @NotBlank(message = "菜单编码不能为空")
    private String menuCode;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 菜单类型（DIR-目录，MENU-菜单，BUTTON-按钮）
     */
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /**
     * 前端路由（如 /sys/user）
     */
    private String routePath;

    /**
     * 前端组件（可选）
     */
    private String component;

    /**
     * 图标（可选）
     */
    private String icon;

    /**
     * 是否显示（0-隐藏，1-显示）
     */
    private Integer visible;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;

}