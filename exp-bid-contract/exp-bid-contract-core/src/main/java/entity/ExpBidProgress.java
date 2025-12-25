package entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 投标进度跟踪表，对应 exp_bid_progress
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_progress")
@TableName("exp_bid_progress")
public class ExpBidProgress {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "progress_id")
    private Long progressId;

    // 投标ID，关联 exp_bid
    @Column(name = "bid_id")
    private Long bidId;

    // 阶段编码（如 SIGNUP、DOC_PREP、SUBMIT、OPENING 等）
    @Column(name = "stage_code", length = 50)
    private String stageCode;

    // 阶段名称（报名、编制投标文件、提交、开标、评标等）
    @Column(name = "stage_name", length = 100)
    private String stageName;

    // 计划开始时间
    @Column(name = "planned_start_time")
    private LocalDateTime plannedStartTime;

    // 计划结束时间
    @Column(name = "planned_end_time")
    private LocalDateTime plannedEndTime;

    // 实际开始时间
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    // 实际结束时间
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    // 阶段状态（未开始、进行中、已完成、延期）
    @Column(name = "status", length = 32)
    private String status;

    // 责任人用户ID，关联账号信息
    @Column(name = "responsible_user_id")
    private Long responsibleUserId;

    // 责任部门ID，关联部门管理
    @Column(name = "responsible_dept_id")
    private Long responsibleDeptId;

    // 延期原因
    @Column(name = "delay_reason", length = 500)
    private String delayReason;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;

    // 阶段顺序号
    @Column(name = "sort_no")
    private Integer sortNo;
}