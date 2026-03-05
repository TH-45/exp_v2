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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wf_task")
@TableName("wf_task")
public class WfTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "instance_id")
    private Long instanceId;

    @Column(name = "node_id")
    private Long nodeId;

    @Column(name = "candidate_type")
    private String candidateType;

    @Column(name = "candidate_id")
    private String candidateId;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "action")
    private String action;

    @Column(name = "opinion")
    private String opinion;

    @Column(name = "is_done")
    private Integer isDone;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;
}
