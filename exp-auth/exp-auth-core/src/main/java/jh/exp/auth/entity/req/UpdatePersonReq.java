package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新人员请求对象
 */
@Data
public class UpdatePersonReq {

    /**
     * 人员ID
     */
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 人员工号/编号
     */
    @NotBlank(message = "人员工号不能为空")
    private String personCode;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String personName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
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
    @NotNull(message = "所属组织不能为空")
    private Long orgId;

    /**
     * 主岗位ID
     */
    @NotNull(message = "所属岗位不能为空")
    private Long postId;

    /**
     * 绑定账号ID
     */
    private Long accountId;

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
     * 备注
     */
    private String remark;
}
