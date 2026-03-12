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
 * 合同主表，对应 exp_contract
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_contract")
@TableName("exp_contract")
public class Contract {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "contract_id")
    private Long contractId;

    // 合同编号
    @Column(name = "contract_code", length = 100)
    private String contractCode;

    // 合同名称
    @Column(name = "contract_name", length = 200)
    private String contractName;

    // 合同类型（工程合同、采购合同、服务合同等）
    @Column(name = "contract_type", length = 50)
    private String contractType;

    // 合同类别（框架合同、一次性合同、分包合同等）
    @Column(name = "contract_category", length = 50)
    private String contractCategory;

    // 招标ID，关联 exp_tender
    @Column(name = "tender_id")
    private Long tenderId;

    // 投标ID（中标记录），关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 工程项目ID，关联工程项目服务模块
    @Column(name = "project_id")
    private Long projectId;

    // 甲方单位ID，关联企业信息（内部单位）
    @Column(name = "purchaser_id")
    private Long purchaserId;

    // 乙方单位ID，关联企业外部基础信息（合作单位/供应商）
    @Column(name = "supplier_id")
    private Long supplierId;

    // 合同签署日期
    @Column(name = "sign_date")
    private LocalDate signDate;

    // 合同生效日期
    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    // 合同结束日期/到期日
    @Column(name = "end_date")
    private LocalDate endDate;

    // 合同总金额（含税）
    @Column(name = "amount_total", precision = 20, scale = 2)
    private BigDecimal amountTotal;

    // 合同金额（不含税）
    @Column(name = "amount_without_tax", precision = 20, scale = 2)
    private BigDecimal amountWithoutTax;

    // 合同默认税率
    @Column(name = "tax_rate_default", precision = 6, scale = 4)
    private BigDecimal taxRateDefault;

    // 币种
    @Column(name = "currency", length = 10)
    private String currency;

    // 付款条件（文字说明，如“预付款+进度款+尾款”等）
    @Column(name = "pay_terms", columnDefinition = "TEXT")
    private String payTerms;

    // 结算方式（按月结算、按节点结算、一次性结算等）
    @Column(name = "settle_mode", length = 50)
    private String settleMode;

    // 合同状态（起草中、审核中、待签署、履行中、已完成、已终止、已作废等）
    @Column(name = "status", length = 32)
    private String status;

    // 是否已经归档（0/1）
    @Column(name = "archive_flag")
    private Integer archiveFlag;

    // 归档时间
    @Column(name = "archive_time")
    private LocalDateTime archiveTime;

    // 签订人用户ID，关联账号信息表（拟签阶段选择「签订」时记录）
    @Column(name = "sign_user_id")
    private Long signUserId;

    // 签订时间（拟签阶段选择「签订」时记录）
    @Column(name = "sign_time")
    private LocalDateTime signTime;

    // 创建人用户ID，关联账号信息表
    @Column(name = "created_by")
    private Long createdBy;

    // 创建人部门ID，关联部门管理
    @Column(name = "created_dept_id")
    private Long createdDeptId;

    // 创建人岗位ID，关联岗位管理
    @Column(name = "created_post_id")
    private Long createdPostId;

    /** 业务员人员ID，创建时通过人员选择器选择 */
    @Column(name = "salesman_person_id")
    private Long salesmanPersonId;

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
