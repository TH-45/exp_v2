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
 * 定标结果表，对应 exp_bid_award_result
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_award_result")
@TableName("exp_bid_award_result")
public class ExpBidAwardResult {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "award_id")
    private Long awardId;

    // 招标项目ID，关联 exp_tender
    @Column(name = "tender_id")
    private Long tenderId;

    // 中标投标ID，关联 exp_bid
    @Column(name = "winning_bid_id")
    private Long winningBidId;

    // 中标单位ID，关联企业外部信息
    @Column(name = "winning_supplier_id")
    private Long winningSupplierId;

    // 中标单位名称
    @Column(name = "winning_supplier_name", length = 200)
    private String winningSupplierName;

    // 中标金额
    @Column(name = "winning_amount", precision = 20, scale = 2)
    private BigDecimal winningAmount;

    // 币种
    @Column(name = "currency", length = 10)
    private String currency;

    // 中标通知书编号
    @Column(name = "award_notice_no", length = 100)
    private String awardNoticeNo;

    // 中标通知书发送时间
    @Column(name = "award_notice_send_time")
    private LocalDateTime awardNoticeSendTime;

    // 合同签订截止时间
    @Column(name = "contract_sign_deadline")
    private LocalDateTime contractSignDeadline;

    // 实际合同签订时间
    @Column(name = "actual_contract_sign_time")
    private LocalDateTime actualContractSignTime;

    // 定标状态（待定标、已定标、已发中标通知、已签订合同、已放弃）
    @Column(name = "award_status", length = 32)
    private String awardStatus;

    // 定标决策人ID
    @Column(name = "decision_maker_id")
    private Long decisionMakerId;

    // 定标决策时间
    @Column(name = "decision_time")
    private LocalDateTime decisionTime;

    // 定标意见
    @Column(name = "award_opinion", columnDefinition = "TEXT")
    private String awardOpinion;

    // 是否需要重新招标（0/1）
    @Column(name = "need_retender")
    private Integer needRetender;

    // 重新招标原因
    @Column(name = "retender_reason", length = 500)
    private String retenderReason;

    // 创建人用户ID，关联账号信息
    @Column(name = "created_by")
    private Long createdBy;

    // 创建人部门ID，关联部门管理
    @Column(name = "created_dept_id")
    private Long createdDeptId;

    // 创建人岗位ID，关联岗位管理
    @Column(name = "created_post_id")
    private Long createdPostId;

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