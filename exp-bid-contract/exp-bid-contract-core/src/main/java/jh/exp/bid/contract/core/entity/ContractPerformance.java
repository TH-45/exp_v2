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

/**
 * 合同履约跟踪表，对应 exp_contract_performance
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_contract_performance")
@TableName("exp_contract_performance")
public class ContractPerformance {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "perf_id")
    private Long perfId;

    // 合同ID，关联 exp_contract
    @Column(name = "contract_id")
    private Long contractId;

    // 履约项类型（付款节点、交付节点、服务节点、里程碑等）
    @Column(name = "item_type", length = 50)
    private String itemType;

    // 履约项名称（如“预付款”、“设备交付”、“验收通过”等）
    @Column(name = "item_name", length = 200)
    private String itemName;

    // 计划完成日期
    @Column(name = "plan_date")
    private LocalDate planDate;

    // 计划金额（对应付款或结算金额，可为空）
    @Column(name = "plan_amount", precision = 20, scale = 2)
    private BigDecimal planAmount;

    // 实际完成日期
    @Column(name = "actual_date")
    private LocalDate actualDate;

    // 实际金额（可为空）
    @Column(name = "actual_amount", precision = 20, scale = 2)
    private BigDecimal actualAmount;

    // 履约状态（未开始、进行中、已完成、已延期、已取消等）
    @Column(name = "status", length = 32)
    private String status;

    // 关联工程项目ID（如需要到项目维度跟踪）
    @Column(name = "related_project_id")
    private Long relatedProjectId;

    // 关联项目阶段/里程碑名称（可选）
    @Column(name = "related_stage", length = 200)
    private String relatedStage;

    // 责任人用户ID，关联账号信息
    @Column(name = "responsible_user_id")
    private Long responsibleUserId;

    // 责任部门ID，关联部门管理
    @Column(name = "responsible_dept_id")
    private Long responsibleDeptId;

    // 延期原因说明
    @Column(name = "delay_reason", length = 500)
    private String delayReason;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}
