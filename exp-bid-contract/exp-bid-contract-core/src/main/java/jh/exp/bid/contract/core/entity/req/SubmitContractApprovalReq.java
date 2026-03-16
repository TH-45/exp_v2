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

    private Long contractId;

    //动作
    @NotNull(message = "动作不能为空")
    private String action;

}
