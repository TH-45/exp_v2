package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号状态变更请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatusReq {

    /**
     * 账号ID
     */
    @NotNull(message = "账号ID不能为空")
    private Long accountId;

    /**
     * 账号状态
     * ENABLED  - 启用
     * DISABLED - 停用
     * LOCKED   - 锁定
     */
    @NotBlank(message = "账号状态不能为空")
    private String status;

    /**
     * 操作备注
     */
    private String remark;

    public AccountStatusReq(Long accountId, String status) {
        this.accountId = accountId;
        this.status = status;
    }
}
