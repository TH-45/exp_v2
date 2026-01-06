package jh.exp.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 账号信息表
 * 表名：exp_account
 *
 * 说明：
 * - 系统登录账号的核心表
 * - 一个账号通常关联一个人员（person）
 * - 支持状态控制、登录风控、强制改密等安全能力
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_account")
@TableName("exp_account")
public class Account {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.ASSIGN_ID)
    @Column(name = "account_id")
    private Long accountId;

    /**
     * 登录账号名（唯一）
     * 如：用户名、工号等
     */
    @Column(name = "account_name", nullable = false, unique = true, length = 64)
    private String accountName;

    /**
     * 账号显示名称
     * 一般与人员姓名一致，用于界面展示（可冗余）
     */
    @Column(name = "account_display", length = 100)
    private String accountDisplay;

    /**
     * 密码加密后的摘要
     * 不允许明文存储
     */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /**
     * 密码加盐值
     * 可选字段，用于增强密码安全性
     */
    @Column(name = "password_salt", length = 64)
    private String passwordSalt;

    /**
     * 预留手机号
     * 用于登录验证、找回密码等
     */
    @Column(name = "mobile", length = 32)
    private String mobile;

    /**
     * 预留邮箱
     * 用于登录验证、找回密码等
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 关联人员ID
     * 对应 exp_person.person_id
     * 一般为一人一账号
     */
    @Column(name = "person_id")
    private Long personId;

    /**
     * 所属主部门 / 组织ID
     * 关联 exp_org_unit.org_id
     */
    @Column(name = "org_id")
    private Long orgId;

    /**
     * 主岗位ID
     * 关联 exp_post.post_id
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * 账号状态
     * ENABLED  - 启用
     * DISABLED - 停用
     * LOCKED   - 锁定
     * INIT     - 初始状态（待首次登录修改密码）
     */
    @Column(name = "status", length = 32)
    private String status;

    /**
     * 连续登录失败次数
     * 用于风控策略（如自动锁定）
     */
    @Column(name = "login_fail_count")
    private Integer loginFailCount;

    /**
     * 最近一次登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最近一次登录IP地址
     */
    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    /**
     * 最近一次密码修改时间
     */
    @Column(name = "pwd_last_change_time")
    private LocalDateTime pwdLastChangeTime;

    /**
     * 是否需要在登录后强制修改密码
     * 0-否，1-是
     */
    @Column(name = "need_change_pwd")
    private Boolean needChangePwd;

    /**
     * 创建人账号ID
     * 关联账号信息表 account_id
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 备注说明
     */
    @Column(name = "remark", length = 500)
    private String remark;
}