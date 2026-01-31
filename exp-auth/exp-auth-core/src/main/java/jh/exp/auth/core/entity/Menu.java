package jh.exp.auth.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单表，对应 exp_menu
 * 用于存储系统菜单、目录和按钮权限信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_menu")
@TableName("exp_menu")
public class Menu {
    // 主键ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "menu_id")
    private Long menuId;

    // 父菜单ID（根为0或NULL），关联 exp_menu.menu_id
    @Column(name = "parent_menu_id")
    private Long parentMenuId;

    // 菜单编码（唯一）
    @Column(name = "menu_code", nullable = false, unique = true, length = 64)
    private String menuCode;

    // 菜单名称
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    // 类型（DIR-目录，MENU-菜单，BUTTON-按钮）
    @Column(name = "menu_type", nullable = false, length = 20)
    private String menuType;

    // 前端路由（如 /sys/user）
    @Column(name = "route_path", length = 200)
    private String routePath;

    // 前端组件（可选）
    @Column(name = "component", length = 200)
    private String component;

    // 图标（可选）
    @Column(name = "icon", length = 100)
    private String icon;

    // 是否显示（0/1）
    @Column(name = "visible")
    private Integer visible;

    // 状态（ENABLED/DISABLED）
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // 排序号
    @Column(name = "sort_no")
    private Integer sortNo;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;

}
