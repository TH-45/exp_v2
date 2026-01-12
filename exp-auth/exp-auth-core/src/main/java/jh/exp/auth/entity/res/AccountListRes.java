package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 账号列表响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountListRes {

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
     * 人员ID
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
     * 组织代码
     */
    private String orgCode;

    /**
     * 组织名称（部门/公司/项目部名称）
     */
    private String orgName;

    /**
     * 状态
     */
    private String status;

    /**
     * 岗位代码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 创建人姓名
     */
    private String createdName;

    /**
     * 最近一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
