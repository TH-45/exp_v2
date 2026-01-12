package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建评标结果请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEvaluationResultReq {

    /**
     * 评标委员会ID
     */
    @NotNull(message = "评标委员会ID不能为空")
    private Long committeeId;

    /**
     * 投标ID
     */
    @NotNull(message = "投标ID不能为空")
    private Long bidId;

    /**
     * 技术评分总分
     */
    private BigDecimal technicalScore;

    /**
     * 商务评分总分
     */
    private BigDecimal businessScore;

    /**
     * 综合评分总分
     */
    private BigDecimal comprehensiveScore;

    /**
     * 最终得分
     */
    private BigDecimal finalScore;

    /**
     * 名次排序
     */
    private Integer ranking;

    /**
     * 是否推荐中标（0/1）
     */
    private Integer isRecommended;

    /**
     * 评标结论
     */
    private String evaluationConclusion;

    /**
     * 评标意见
     */
    private String evaluationOpinion;

    /**
     * 评标结果状态
     */
    private String resultStatus;

    /**
     * 备注
     */
    private String remark;
}