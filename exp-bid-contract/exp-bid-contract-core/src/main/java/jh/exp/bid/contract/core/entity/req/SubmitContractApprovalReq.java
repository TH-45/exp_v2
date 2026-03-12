package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合同提交审批请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitContractApprovalReq {

    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    /** 流程定义ID（可选，不传则按 busType=contract 取第一个启用流程） */
    private Long procDefId;

    /** 流程编码（可选） */
    private String procCode;
}
