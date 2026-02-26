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

/**
 * 项目进度计划表，对应 exp_project_schedule
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project_schedule")
@TableName("exp_project_schedule")
public class ProjectSchedule {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "schedule_id")
    private Long scheduleId;

    // 项目ID
    @Column(name = "project_id")
    private Long projectId;

    // 上级计划ID（WBS 树形分解）
    @Column(name = "parent_schedule_id")
    private Long parentScheduleId;

    // 进度计划名称
    @Column(name = "schedule_name")
    private String scheduleName;

    // 是否里程碑节点（0/1）
    @Column(name = "milestone_flag")
    private Integer milestoneFlag;

    // 计划开始日期
    @Column(name = "plan_start_date")
    private LocalDate planStartDate;

    // 计划结束日期
    @Column(name = "plan_end_date")
    private LocalDate planEndDate;

    // 实际开始日期
    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    // 实际结束日期
    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    // 计划工期（天）
    @Column(name = "plan_duration_days")
    private Integer planDurationDays;

    // 实际工期（天）
    @Column(name = "actual_duration_days")
    private Integer actualDurationDays;

    // 当前完成百分比（0-100）
    @Column(name = "progress_percent")
    private BigDecimal progressPercent;

    // 状态
    @Column(name = "status")
    private String status;

    // 责任人用户ID
    @Column(name = "responsible_user_id")
    private Long responsibleUserId;

    // 责任部门/项目部ID
    @Column(name = "responsible_org_id")
    private Long responsibleOrgId;

    // 排序号
    @Column(name = "sort_no")
    private Integer sortNo;

    // 备注
    @Column(name = "remark")
    private String remark;
}
