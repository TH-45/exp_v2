package jh.exp.bid.contract.entity;

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
 * 评标打分记录表，对应 exp_bid_evaluation_score
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_evaluation_score")
@TableName("exp_bid_evaluation_score")
public class ExpBidEvaluationScore {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "score_id")
    private Long scoreId;

    // 评标委员会ID，关联 exp_bid_evaluation_committee
    @Column(name = "committee_id")
    private Long committeeId;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 评标专家用户ID，关联 exp_bid_evaluation_member
    @Column(name = "expert_user_id")
    private Long expertUserId;

    // 评分类型（技术评分、商务评分、综合评分等）
    @Column(name = "score_type", length = 50)
    private String scoreType;

    // 评分项目（技术方案、价格、工期、服务等）
    @Column(name = "score_item", length = 100)
    private String scoreItem;

    // 分值（0-100分）
    @Column(name = "score_value", precision = 5, scale = 2)
    private BigDecimal scoreValue;

    // 权重百分比（0-100）
    @Column(name = "weight_percentage", precision = 5, scale = 2)
    private BigDecimal weightPercentage;

    // 加权得分
    @Column(name = "weighted_score", precision = 7, scale = 2)
    private BigDecimal weightedScore;

    // 评分意见
    @Column(name = "score_comment", columnDefinition = "TEXT")
    private String scoreComment;

    // 评分时间
    @Column(name = "score_time")
    private LocalDateTime scoreTime;

    // 评分状态（草稿、已提交）
    @Column(name = "score_status", length = 32)
    private String scoreStatus;

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