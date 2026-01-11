package jh.exp.bid.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 投标人资格审核表，对应 exp_tender_bidder_review
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_tender_bidder_review")
@TableName("exp_tender_bidder_review")
public class ExpTenderBidderReview {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "review_id")
    private Long reviewId;

    // 招标ID，关联 exp_tender
    @Column(name = "tender_id")
    private Long tenderId;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 投标人/供应商ID，关联企业外部基础信息（合作单位/供应商）
    @Column(name = "supplier_id")
    private Long supplierId;

    // 审核阶段（资格预审、资格后审等）
    @Column(name = "review_stage", length = 50)
    private String reviewStage;

    // 审核结果（通过、不通过、待定等）
    @Column(name = "review_result", length = 32)
    private String reviewResult;

    // 资格审核评分（可选）
    @Column(name = "review_score", precision = 5, scale = 2)
    private Double reviewScore;

    // 审核意见说明
    @Column(name = "review_opinion", length = 1000)
    private String reviewOpinion;

    // 审核人用户ID，关联账号信息
    @Column(name = "reviewer_user_id")
    private Long reviewerUserId;

    // 审核人部门ID，关联部门管理
    @Column(name = "reviewer_dept_id")
    private Long reviewerDeptId;

    // 审核时间
    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}