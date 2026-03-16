package jh.exp.process.core.entity;

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

import java.time.LocalDateTime;

/**
 * 任务
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wf_task")
@TableName("wf_task")
public class WfTask {

    /**
     * 任务 ID，主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "task_id")
    private Long taskId;

    /**
     * 流程实例 ID
     */
    @Column(name = "instance_id")
    private Long instanceId;

    /**
     * 节点 ID
     */
    @Column(name = "node_id")
    private Long nodeId;

    /**
     * 候选人类型
     */
    @Column(name = "candidate_type")
    private String candidateType;

    /**
     * 候选人 ID
     */
    @Column(name = "candidate_id")
    private String candidateId;

    /**
     * 处理人 ID
     */
    @Column(name = "handler_id")
    private Long handlerId;

    /**
     * 操作动作 （APPROVE-审批, AGREE-同意, REJECT-驳回, CLOSE-强制关闭）
     */
    @Column(name = "action")
    private String action;

    /**
     * 审批意见
     */
    @Column(name = "opinion")
    private String opinion;

    /**
     * 是否完成 (0: 未完成, 1: 已完成)
     */
    @Column(name = "is_done")
    private Integer isDone;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    @Column(name = "finish_time")
    private LocalDateTime finishTime;
}
