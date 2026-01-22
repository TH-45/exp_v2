package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员详情响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonDetailRes {

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员工号/编号
     */
    private String personCode;

    /**
     * 姓名
     */
    private String personName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 身份证号/证件号
     */
    private String idCardNo;

    /**
     * 职务
     */
    private String jobTitle;

    /**
     * 主属组织/部门ID
     */
    private Long orgId;

    /**
     * 组织代码
     */
    private String orgCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 主岗位ID
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
     * 绑定账号ID
     */
    private Long accountId;

    /**
     * 账号名称
     */
    private String accountName;

    /**
     * 账号显示名称
     */
    private String accountDisplay;

    /**
     * 人员状态
     */
    private String status;

    /**
     * 入职日期
     */
    private LocalDate entryDate;

    /**
     * 离职日期
     */
    private LocalDate leaveDate;

    /**
     * 是否外部人员/合作方
     */
    private Integer isExternal;

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
