package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 账号详情响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetailRes {

    /**
     * 账号ID
     */
    private Long accountId;

    /**
     * 登录账号名
     */
    private String accountName;

    /**
     * 账号显示名称
     */
    private String accountDisplay;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 关联人员ID
     */
    private Long personId;

    /**
     * 员工编号
     */
    private String personCode;

    /**
     * 姓名
     */
    private String personName;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 组织代码
     */
    private String orgCode;

    /**
     * 组织名称（部门/公司/项目部名称）
     */
    private String orgName;

    /**
     * 岗位ID
     */
    private Long postId;

    /**
     * 岗位代码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 账号状态
     */
    private String status;

    /**
     * 连续登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 最近一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最近一次登录IP
     */
    private LocalDateTime lastLoginIp;

    /**
     * 最近一次密码修改时间
     */
    private LocalDateTime pwdLastChangeTime;

    /**
     * 是否登录后强制修改密码
     */
    private Boolean needChangePwd;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 备注
     */
    private String remark;
}
