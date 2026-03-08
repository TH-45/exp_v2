package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定标流程决策请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AwardProcessDecisionReq {

    /**
     * 定标结果ID
     */
    @NotNull(message = "定标结果ID不能为空")
    private Long awardId;

    /**
     * 流程动作：APPROVE / REJECT
     */
    @NotBlank(message = "流程动作不能为空")
    private String action;

    /**
     * 驳回原因码（REJECT 时可传，用于双轨回退）
     * 例如：SCORE_REWORK、DOC_FIX
     */
    private String rejectReasonCode;

    /**
     * 审批意见
     */
    private String opinion;
}
