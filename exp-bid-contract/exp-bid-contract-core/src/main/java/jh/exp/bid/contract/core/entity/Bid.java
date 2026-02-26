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
 * 投标主表，对应 exp_bid
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid")
@TableName("exp_bid")
public class Bid {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "bid_id")
    private Long bidId;

    // 招标项目ID
    @Column(name = "tender_id")
    private Long tenderId;

    // 投标单位ID，关联企业外部基础信息（合作单位/供应商）
    @Column(name = "supplier_id")
    private Long supplierId;

    // 投标编号（公司内部编码）
    @Column(name = "bid_code", length = 100)
    private String bidCode;

    // 投标名称
    @Column(name = "bid_name", length = 200)
    private String bidName;

    // 投标总报价金额
    @Column(name = "bid_total_amount", precision = 20, scale = 2)
    private BigDecimal bidTotalAmount;

    // 币种
    @Column(name = "currency", length = 10)
    private String currency;

    // 投标提交时间
    @Column(name = "bid_submit_time")
    private LocalDateTime bidSubmitTime;

    // 投标状态（准备、已提交、评审中、中标、未中标、放弃等）
    @Column(name = "bid_status", length = 32)
    private String bidStatus;

    // 是否中标标识（0/1）
    @Column(name = "win_flag")
    private Integer winFlag;

    // 中标通知书编号
    @Column(name = "win_notice_no", length = 100)
    private String winNoticeNo;

    // 合同ID，关联合同管理模块合同表
    @Column(name = "contract_id")
    private Long contractId;

    // 工程项目ID，关联工程项目服务模块
    @Column(name = "project_id")
    private Long projectId;

    // 创建人用户ID，关联账号信息表
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
