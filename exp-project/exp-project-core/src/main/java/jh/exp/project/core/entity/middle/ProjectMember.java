package jh.exp.project.core.entity.middle;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目成员表
 * 对应表：exp_project_member
 */
@Entity
@Table(name = "exp_project_member")
@TableName("exp_project_member")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 项目ID，关联 exp_project.project_id
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 人员ID，关联 exp_person.person_id
     */
    @Column(name = "person_id", nullable = false)
    private Long personId;

    /**
     * 任职部门ID（快照）
     */
    @Column(name = "org_id")
    private Long orgId;

    /**
     * 任职岗位ID（快照）
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * 任职开始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 任职结束日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 状态
     */
    @Column(name = "status", length = 32)
    private String status;

    /**
     * 创建人
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

}