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
 * 人员项目参与记录表，对应 exp_person_project_rel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_person_project_rel")
@TableName("exp_person_project_rel")
public class PersonProjectRel {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "id")
    private Long id;

    // 人员ID，关联 exp_person.person_id
    @Column(name = "person_id")
    private Long personId;

    // 项目ID，关联工程项目服务模块项目表
    @Column(name = "project_id")
    private Long projectId;

    // 在项目中的角色
    @Column(name = "project_role")
    private String projectRole;

    // 所属组织/项目部ID，关联 exp_org_unit.org_id（可选）
    @Column(name = "org_id")
    private Long orgId;

    // 项目中对应岗位ID，关联 exp_post.post_id（可选）
    @Column(name = "post_id")
    private Long postId;

    // 参与项目开始时间
    @Column(name = "start_date")
    private LocalDate startDate;

    // 参与项目结束时间
    @Column(name = "end_date")
    private LocalDate endDate;

    // 状态（ONGOING-进行中，FINISHED-已结束）
    @Column(name = "status")
    private String status;

    // 备注
    @Column(name = "remark")
    private String remark;
}
