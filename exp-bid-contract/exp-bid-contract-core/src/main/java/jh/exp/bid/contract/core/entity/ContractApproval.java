package jh.exp.bid.contract.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合同审核/审批表，对应 exp_contract_approval
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_contract_approval")
@TableName("exp_contract_approval")
public class ContractApproval {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "approval_id")
    private Long approvalId;

    // 合同ID，关联 exp_contract
    @Column(name = "contract_id")
    private Long contractId;

    // 审批节点编码（如 DRAFT、DEPT_LEADER、LEGAL、FINANCE、BOSS 等）
    @Column(name = "node_code", length = 50)
    private String nodeCode;

    // 审批节点名称（部门负责人审核、法务审核、财务审核、总经理审批等）
    @Column(name = "node_name", length = 200)
    private String nodeName;

    // 审批人用户ID，关联账号信息
    @Column(name = "approver_user_id")
    private Long approverUserId;

    // 审批人部门ID，关联部门管理
    @Column(name = "approver_dept_id")
    private Long approverDeptId;

    // 审批结果（同意、退回、驳回、转审等）
    @Column(name = "approve_result", length = 32)
    private String approveResult;

    // 审批意见/说明
    @Column(name = "approve_opinion", columnDefinition = "TEXT")
    private String approveOpinion;

    // 审批时间
    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    // 节点状态（待审批、已审批、已跳过等）
    @Column(name = "status", length = 32)
    private String status;

    // 审批顺序号
    @Column(name = "sort_no")
    private Integer sortNo;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}
