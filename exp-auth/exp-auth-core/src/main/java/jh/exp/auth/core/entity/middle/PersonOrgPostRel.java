package jh.exp.auth.core.entity.middle;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 人员-组织-岗位关联表，对应 exp_person_org_post_rel
 * 用于建立人员、组织和岗位之间的任职关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "exp_person_org_post_rel")
@TableName("exp_person_org_post_rel")
public class PersonOrgPostRel {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "id")
    private Long id;

    /**
     * 人员ID，关联 exp_person.person_id
     */
    @Column(name = "person_id", nullable = false)
    private Long personId;

    /**
     * 组织/部门ID，关联 exp_org_unit.org_id
     */
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    /**
     * 岗位ID，关联 exp_post.post_id
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 关联默认角色ID（可选，用于在该部门+岗位下推荐的角色）
     */
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 是否主任职（0/1，用于区分主岗位与兼岗）
     * 0: 兼职岗位，1: 主要岗位
     */
    @Column(name = "is_primary")
    private Integer isPrimary;

    /**
     * 任职开始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 任职结束日期（在任中可为空）
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 状态（ON-在任，OFF-已结束，tbd-待定）
     */
    @Column(name = "status", length = 10)
    private String status;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

}