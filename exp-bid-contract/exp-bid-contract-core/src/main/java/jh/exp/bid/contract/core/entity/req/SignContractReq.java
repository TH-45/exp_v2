package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 合同签订/不签订请求（统一接口，通过 action 区分业务）
 * 拟签阶段：签订 → 正常归档；不签订+变更 → 返回起草；不签订+不变更 → 异常归档
 */
@Data
public class SignContractReq {

    /** 合同ID */
    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    /**
     * 操作类型：SIGN-签订，UNSIGN-不签订
     */
    @NotNull(message = "操作类型不能为空")
    private String action;

    /** 签订意见（签订与不签订均可填写） */
    private String opinion;

    /**
     * 是否变更（仅 action=UNSIGN 时有效）
     * true-返回合同起草，false-异常合同归档
     */
    private Boolean needChange;
}
