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

import java.time.LocalDateTime;

/**
 * 项目岗位配置表，对应 exp_project_role_cfg
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_project_role_cfg")
@TableName("exp_project_role_cfg")
public class ProjectRoleCfg {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "cfg_id")
    private Long cfgId;

    // 项目ID，关联工程项目主表
    @Column(name = "project_id")
    private Long projectId;

    // 项目角色编码（如 PM、TECH_LEAD）
    @Column(name = "role_code")
    private String roleCode;

    // 项目角色名称
    @Column(name = "role_name")
    private String roleName;

    // 该角色计划配置人数
    @Column(name = "required_count")
    private Integer requiredCount;

    // 是否必须内部人员（0/1）
    @Column(name = "must_internal")
    private Integer mustInternal;

    // 角色重要级别（1 高、2 中、3 低）
    @Column(name = "priority_level")
    private Integer priorityLevel;

    // 状态（ENABLED-启用，DISABLED-停用）
    @Column(name = "status")
    private String status;

    // 备注
    @Column(name = "remark")
    private String remark;

    // 创建人
    @Column(name = "created_by")
    private Long createdBy;

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
