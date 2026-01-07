package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除账号请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAccountReq {

    /**
     * 账号ID
     */
    @NotNull(message = "账号ID不能为空")
    private Long accountId;

    /**
     * 操作备注
     */
    private String remark;
}
