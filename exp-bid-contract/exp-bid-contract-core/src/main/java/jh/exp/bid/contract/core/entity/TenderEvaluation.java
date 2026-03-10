package jh.exp.bid.contract.core.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评标流程记录表
 * 表名：exp_tender_evaluation
 */
@Data
public class TenderEvaluation {

    /**
     * 主键ID
     */
    private Long evalId;

    /**
     * 招标ID
     */
    private Long tenderId;

    /**
     * 投标ID
     */
    private Long bidId;

    /**
     * 评标轮次（1、2等）
     */
    private Integer roundNo;

    /**
     * 评标阶段编码
     * QUALIFY 资格评审
     * TECH 技术评审
     * BUSINESS 商务评审
     * COMPREHENSIVE 综合评审
     */
    private String stageCode;

    /**
     * 评标阶段名称
     */
    private String stageName;

    /**
     * 技术得分
     */
    private BigDecimal techScore;

    /**
     * 商务得分
     */
    private BigDecimal businessScore;

    /**
     * 综合得分
     */
    private BigDecimal totalScore;

    /**
     * 名次
     */
    private Integer rank;

    /**
     * 评审结论
     * PASS 通过
     * ELIMINATED 淘汰
     * CANDIDATE 候选中标人
     * WINNER 中标人
     */
    private String result;

    /**
     * 评审负责人用户ID
     */
    private Long evaluatorUserId;

    /**
     * 评审时间
     */
    private LocalDateTime evalTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

}