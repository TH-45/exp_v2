package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新合同状态请求
 * 供流程引擎或内部服务调用，按流程结果更新合同状态（如驳回→起草，通过→拟签）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateContractStatusReq {

    /** 合同ID */
    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    /** 合同状态，如 DRAFT、UNDER_REVIEW、PENDING_SIGN、ARCHIVED 等 */
    @NotBlank(message = "合同状态不能为空")
    private String status;
}
