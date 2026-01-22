package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 重置密码请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordReq {

    /**
     * 账号ID列表
     */
    @NotEmpty(message = "账号ID列表不能为空")
    private List<Long> accountIds;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /**
     * 操作备注
     */
    private String remark;
}
