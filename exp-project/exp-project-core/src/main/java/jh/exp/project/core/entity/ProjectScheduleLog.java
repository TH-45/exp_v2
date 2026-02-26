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
 * 项目进度变更及更新记录表，对应 exp_project_schedule_log
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project_schedule_log")
@TableName("exp_project_schedule_log")
public class ProjectScheduleLog {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "log_id")
    private Long logId;

    // 进度计划ID，关联 exp_project_schedule.schedule_id
    @Column(name = "schedule_id")
    private Long scheduleId;

    // 项目ID（冗余存储）
    @Column(name = "project_id")
    private Long projectId;

    // 记录时间（进度更新的时间）
    @Column(name = "change_time")
    private LocalDateTime changeTime;

    // 变更前完成百分比
    @Column(name = "before_progress", precision = 7, scale = 2)
    private BigDecimal beforeProgress;

    // 变更后完成百分比
    @Column(name = "after_progress", precision = 7, scale = 2)
    private BigDecimal afterProgress;

    // 原计划结束日期
    @Column(name = "before_plan_end_date")
    private LocalDate beforePlanEndDate;

    // 调整后计划结束日期
    @Column(name = "after_plan_end_date")
    private LocalDate afterPlanEndDate;

    // 变更类型
    @Column(name = "change_type")
    private String changeType;

    // 延期/变更原因说明
    @Column(name = "delay_reason")
    private String delayReason;

    // 操作人用户ID
    @Column(name = "operator_user_id")
    private Long operatorUserId;

    // 备注
    @Column(name = "remark")
    private String remark;
}
