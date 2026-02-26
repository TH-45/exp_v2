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

import java.time.LocalDate;

/**
 * 项目人员配置表，对应 exp_project_staff_assign
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project_staff_assign")
@TableName("exp_project_staff_assign")
public class ProjectStaffAssign {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "id")
    private Long id;

    // 项目ID
    @Column(name = "project_id")
    private Long projectId;

    // 人员ID
    @Column(name = "person_id")
    private Long personId;

    // 所属项目组织/项目部ID
    @Column(name = "org_id")
    private Long orgId;

    // 项目内岗位ID
    @Column(name = "post_id")
    private Long postId;

    // 项目角色编码
    @Column(name = "project_role_code")
    private String projectRoleCode;

    // 项目角色名称
    @Column(name = "project_role_name")
    private String projectRoleName;

    // 是否项目核心负责人（0/1）
    @Column(name = "is_leader")
    private Integer isLeader;

    // 参与项目开始日期
    @Column(name = "start_date")
    private LocalDate startDate;

    // 参与项目结束日期
    @Column(name = "end_date")
    private LocalDate endDate;

    // 状态（ONGOING-在项目中，FINISHED-已结束）
    @Column(name = "status")
    private String status;

    // 备注
    @Column(name = "remark")
    private String remark;
}
