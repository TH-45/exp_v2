package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建定标结果请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAwardResultReq {

    /**
     * 招标项目ID
     */
    @NotNull(message = "招标项目ID不能为空")
    private Long tenderId;

    /**
     * 中标投标ID
     */
    @NotNull(message = "中标投标ID不能为空")
    private Long winningBidId;

    /**
     * 中标金额
     */
    private BigDecimal winningAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 中标通知书编号
     */
    private String awardNoticeNo;

    /**
     * 中标通知书发送时间
     */
    private LocalDateTime awardNoticeSendTime;

    /**
     * 合同签订截止时间
     */
    private LocalDateTime contractSignDeadline;

    /**
     * 实际合同签订时间
     */
    private LocalDateTime actualContractSignTime;

    /**
     * 定标状态
     */
    private String awardStatus;

    /**
     * 定标决策人ID
     */
    private Long decisionMakerId;

    /**
     * 定标决策时间
     */
    private LocalDateTime decisionTime;

    /**
     * 定标意见
     */
    private String awardOpinion;

    /**
     * 是否需要重新招标（0/1）
     */
    private Integer needRetender;

    /**
     * 重新招标原因
     */
    private String retenderReason;

    /**
     * 备注
     */
    private String remark;
}