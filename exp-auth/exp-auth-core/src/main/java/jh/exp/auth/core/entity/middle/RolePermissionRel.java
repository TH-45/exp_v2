package jh.exp.auth.core.entity.middle;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-权限关联表，对应 docs 中的：
 * 角色-权限关联表 exp_role_permission_rel
 */
@Entity
@Table(name = "exp_role_permission_rel")
@TableName("exp_role_permission_rel")
@Data
public class RolePermissionRel {

    /**
     * 主键ID，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    /**
     * 角色ID，不能为空
     */
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * 权限ID，不能为空
     */
    @Column(name = "perm_id", nullable = false)
    private Long permId;

    /**
     * 授权类型，最大长度32字符
     */
    @Column(name = "grant_type", length = 32)
    private String grantType;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 备注信息，最大长度500字符
     */
    @Column(name = "remark", length = 500)
    private String remark;


}

