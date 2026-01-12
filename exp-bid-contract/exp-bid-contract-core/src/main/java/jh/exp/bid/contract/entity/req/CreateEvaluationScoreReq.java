package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建评标打分请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEvaluationScoreReq {

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
     * 评标专家用户ID
     */
    @NotNull(message = "评标专家用户ID不能为空")
    private Long expertUserId;

    /**
     * 评分类型
     */
    @NotNull(message = "评分类型不能为空")
    private String scoreType;

    /**
     * 评分项目
     */
    @NotNull(message = "评分项目不能为空")
    private String scoreItem;

    /**
     * 分值
     */
    @NotNull(message = "分值不能为空")
    private BigDecimal scoreValue;

    /**
     * 权重百分比
     */
    private BigDecimal weightPercentage;

    /**
     * 评分意见
     */
    private String scoreComment;

    /**
     * 备注
     */
    private String remark;
}