package jh.exp.project.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工程项目主表，对应 exp_project
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project")
@TableName("exp_project")
public class Project {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "project_id")
    private Long projectId;

    // 项目编码（内部唯一）
    @Column(name = "project_code")
    private String projectCode;

    // 项目名称
    @Column(name = "project_name")
    private String projectName;

    // 项目类型（工程/服务/采购等）
    @Column(name = "project_type")
    private String projectType;

    // 项目状态
    @Column(name = "project_status")
    private String projectStatus;

    // 项目所属单位（本公司）
    @Column(name = "owner_company_id")
    private Long ownerCompanyId;

    // 业主/甲方单位ID
    @Column(name = "customer_company_id")
    private Long customerCompanyId;

    // 主合同ID（可为空）
    @Column(name = "contract_id")
    private Long contractId;

    // 招标ID（可为空）
    @Column(name = "tender_id")
    private Long tenderId;

    // 中标投标ID（可为空）
    @Column(name = "bid_id")
    private Long bidId;

    // 项目开始日期
    @Column(name = "start_date")
    private LocalDate startDate;

    // 计划完成日期
    @Column(name = "plan_end_date")
    private LocalDate planEndDate;

    // 实际完成日期
    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    // 项目负责人（人员ID）
    @Column(name = "manager_person_id")
    private Long managerPersonId;

    // 项目负责人账号（冗余）
    @Column(name = "manager_account_id")
    private Long managerAccountId;

    // 项目归属组织/项目部
    @Column(name = "org_id")
    private Long orgId;

    // 项目预算金额
    @Column(name = "budget_amount", precision = 20, scale = 2)
    private BigDecimal budgetAmount;

    // 币种
    @Column(name = "currency")
    private String currency;

    // 创建人
    @Column(name = "created_by")
    private Long createdBy;

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 备注
    @Column(name = "remark")
    private String remark;
}
