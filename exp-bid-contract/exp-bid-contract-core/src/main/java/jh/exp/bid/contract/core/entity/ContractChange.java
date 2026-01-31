package jh.exp.bid.contract.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同变更/补充协议表，对应 exp_contract_change
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_contract_change")
@TableName("exp_contract_change")
public class ContractChange {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "change_id")
    private Long changeId;

    // 原始合同ID，关联 exp_contract
    @Column(name = "contract_id")
    private Long contractId;

    // 变更编号/补充协议编号
    @Column(name = "change_code", length = 100)
    private String changeCode;

    // 变更标题（如“合同金额调整补充协议一”）
    @Column(name = "change_title", length = 200)
    private String changeTitle;

    // 变更类型（金额变更、工期变更、范围变更、条款变更等）
    @Column(name = "change_type", length = 50)
    private String changeType;

    // 变更前合同金额
    @Column(name = "before_amount", precision = 20, scale = 2)
    private BigDecimal beforeAmount;

    // 变更后合同金额
    @Column(name = "after_amount", precision = 20, scale = 2)
    private BigDecimal afterAmount;

    // 金额变更差额
    @Column(name = "amount_diff", precision = 20, scale = 2)
    private BigDecimal amountDiff;

    // 变更前合同结束日期
    @Column(name = "before_end_date")
    private LocalDate beforeEndDate;

    // 变更后合同结束日期
    @Column(name = "after_end_date")
    private LocalDate afterEndDate;

    // 变更原因说明
    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    // 变更协议签署日期
    @Column(name = "sign_date")
    private LocalDate signDate;

    // 变更状态（起草中、审核中、生效、作废等）
    @Column(name = "status", length = 32)
    private String status;

    // 创建人用户ID，关联账号信息
    @Column(name = "created_by")
    private Long createdBy;

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
