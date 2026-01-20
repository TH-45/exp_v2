package jh.exp.auth.entity.middle;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色-菜单关联表，对应 exp_role_menu_rel
 * 用于建立角色与菜单权限的关联关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_role_menu_rel")
@TableName("exp_role_menu_rel")
public class RoleMenuRel {
    // 主键ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "id")
    private Long id;

    // 角色ID，关联 exp_role.role_id
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    // 菜单ID，关联 exp_menu.menu_id
    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    // 创建人用户ID
    @Column(name = "created_by")
    private Long createdBy;

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;

}