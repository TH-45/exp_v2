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
@Table(name = "wf_instance")
@TableName("wf_instance")
public class WfInstance {

    /**
     * 流程实例 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "instance_id")
    private Long instanceId;

    /**
     * 流程定义 ID
     */
    @Column(name = "proc_def_id")
    private Long procDefId;

    /**
     * 业务 ID
     */
    @Column(name = "bus_id")
    private Long busId;

    /**
     * 启动人 ID
     */
    @Column(name = "starter_id")
    private Long starterId;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 状态
     */
    @Column(name = "status")
    private String status;

    /**
     * 关闭人 ID
     */
    @Column(name = "closed_by")
    private Long closedBy;

    /**
     * 关闭原因
     */
    @Column(name = "close_reason")
    private String closeReason;

    //实例标题
    @Column(name = "title")
    private String title;
}
