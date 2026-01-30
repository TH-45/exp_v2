package jh.exp.bid.contract.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评标结果汇总表，对应 exp_bid_evaluation_result
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_evaluation_result")
@TableName("exp_bid_evaluation_result")
public class ExpBidEvaluationResult {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "result_id")
    private Long resultId;

    // 评标委员会ID，关联 exp_bid_evaluation_committee
    @Column(name = "committee_id")
    private Long committeeId;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 投标单位ID，关联企业外部信息
    @Column(name = "supplier_id")
    private Long supplierId;

    // 投标单位名称
    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    // 投标编号
    @Column(name = "bid_code", length = 100)
    private String bidCode;

    // 投标名称
    @Column(name = "bid_name", length = 200)
    private String bidName;

    // 投标总报价
    @Column(name = "bid_total_amount", precision = 20, scale = 2)
    private BigDecimal bidTotalAmount;

    // 币种
    @Column(name = "currency", length = 10)
    private String currency;

    // 技术评分总分
    @Column(name = "technical_score", precision = 7, scale = 2)
    private BigDecimal technicalScore;

    // 商务评分总分
    @Column(name = "business_score", precision = 7, scale = 2)
    private BigDecimal businessScore;

    // 综合评分总分
    @Column(name = "comprehensive_score", precision = 7, scale = 2)
    private BigDecimal comprehensiveScore;

    // 最终得分（综合评分）
    @Column(name = "final_score", precision = 7, scale = 2)
    private BigDecimal finalScore;

    // 名次排序
    @Column(name = "ranking")
    private Integer ranking;

    // 是否推荐中标（0/1）
    @Column(name = "is_recommended")
    private Integer isRecommended;

    // 评标结论（通过、不通过、待定等）
    @Column(name = "evaluation_conclusion", length = 100)
    private String evaluationConclusion;

    // 评标意见
    @Column(name = "evaluation_opinion", columnDefinition = "TEXT")
    private String evaluationOpinion;

    // 评标结果状态（初评、复评、终评）
    @Column(name = "result_status", length = 32)
    private String resultStatus;

    // 评标完成时间
    @Column(name = "evaluation_completed_time")
    private LocalDateTime evaluationCompletedTime;

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}