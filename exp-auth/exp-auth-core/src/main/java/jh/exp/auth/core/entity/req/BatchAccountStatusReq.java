package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量账号状态变更请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchAccountStatusReq {

    /**
     * 账号ID列表
     */
    @NotEmpty(message = "账号ID列表不能为空")
    private List<Long> accountIds;

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
}
