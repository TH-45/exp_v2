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
 * 投标操作日志表，对应 exp_bid_operation_log
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_operation_log")
@TableName("exp_bid_operation_log")
public class ExpBidOperationLog {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "log_id")
    private Long logId;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 操作类型（新增、编辑、提交、撤回、变更状态等）
    @Column(name = "operation_type", length = 50)
    private String operationType;

    // 操作内容描述或JSON（记录关键字段变更）
    @Column(name = "operation_content", columnDefinition = "TEXT")
    private String operationContent;

    // 操作人用户ID，关联账号信息
    @Column(name = "operator_user_id")
    private Long operatorUserId;

    // 操作人部门ID，关联部门管理
    @Column(name = "operator_dept_id")
    private Long operatorDeptId;

    // 操作时间
    @Column(name = "operation_time")
    private LocalDateTime operationTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}