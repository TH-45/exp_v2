package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建账号请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountReq {

    /**
     * 登录账号名（唯一）
     */
    @NotBlank(message = "账号名称不能为空")
    private String accountName;

    /**
     * 账号显示名称
     */
    @NotBlank(message = "账号显示名称不能为空")
    private String accountDisplay;
//
//    /**
//     * 密码
//     */
//    @NotBlank(message = "密码不能为空")
//    private String password;

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
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 所属主部门/组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    /**
     * 主岗位ID
     */
    @NotNull(message = "岗位ID不能为空")
    private Long postId;

    /**
     * 备注
     */
    private String remark;
}
