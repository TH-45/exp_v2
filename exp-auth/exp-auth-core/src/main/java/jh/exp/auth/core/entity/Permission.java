package jh.exp.auth.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限资源表，对应 docs 中的：
 * 权限资源表 exp_permission
 */
@Entity
@Table(name = "exp_permission")
@TableName("exp_permission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    /**
     * 权限ID，主键，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "perm_id")
    private Long permId;

    /**
     * 权限编码，唯一且不可为空，最大长度128
     * 编码编写规则：MENU_XXX_XXX 表示菜单权限（MENU_）
     */
    @Column(name = "perm_code", nullable = false, unique = true, length = 128)
    private String permCode;

    /**
     * 权限名称，不可为空，最大长度100
     */
    @Column(name = "perm_name", nullable = false, length = 100)
    private String permName;

    /**
     * 权限类型，最大长度32
     * 可选值：FUNC-业务能力权限，DATA-数据范围权限
     */
    @Column(name = "perm_type", length = 32)
    private String permType;

    /**
     * 所属功能模块编码，最大长度64
     */
    @Column(name = "module_code", length = 64)
    private String moduleCode;

    /**
     * 权限分组编码，最大长度64
     * 用于权限配置页面分组展示
     */
    @Column(name = "group_code", length = 64)
    private String groupCode;

    /**
     * 权限值，用于细粒度权限控制，最大长度32
     */
    @Column(name = "perm_value", length = 32)
    private String permValue;

    /**
     * 状态，最大长度32
     * 可选值：ENABLED-启用，DISABLED-停用
     */
    @Column(name = "status", length = 32)
    private String status;

    /**
     * 排序号
     */
    @Column(name = "sort_no")
    private Integer sortNo;

    /**
     * 备注，最大长度500
     */
    @Column(name = "remark", length = 500)
    private String remark;


}

