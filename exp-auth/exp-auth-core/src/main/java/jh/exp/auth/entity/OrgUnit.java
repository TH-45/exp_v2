package jh.exp.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 部门与组织架构表，对应 exp_org_unit
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_org_unit")
@TableName("exp_org_unit")
public class OrgUnit {
    // 主键ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "org_id")
    private Long orgId;

    // 上级组织/部门ID（根节点为0或NULL），关联 exp_org_unit.org_id
    @Column(name = "parent_org_id")
    private Long parentOrgId;

    // 组织编码（唯一，用于导入导出/对接）
    @Column(name = "org_code", nullable = false, unique = true, length = 64)
    private String orgCode;

    // 组织名称（部门/公司/项目部名称）
    @Column(name = "org_name", nullable = false, length = 100)
    private String orgName;

    // 组织类型（COMPANY-公司/法人主体；DEPT-部门；PROJECT-项目部/项目组织；OTHER-其他，可扩展）
    @Column(name = "org_type", nullable = false, length = 32)
    private String orgType;

    // 组织路径（如 /1/3/8/，用于快速查询上下级）
    @Column(name = "org_path", length = 500)
    private String orgPath;

    // 层级（根为1，子级递增）
    @Column(name = "org_level")
    private Integer orgLevel;

    // 负责人人员ID（可选），关联人员基础信息表 exp_person.person_id
    @Column(name = "manager_person_id")
    private Long managerPersonId;

    // 联系电话（可选）
    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    // 地址（可选）
    @Column(name = "address", length = 200)
    private String address;

    // 状态（启用：ENABLED，停用：DISABLED）
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    // 排序号（同级组织显示顺序）
    @Column(name = "sort_no")
    private Integer sortNo;

    // 创建人用户ID，关联账号信息表
    @Column(name = "created_by")
    private Long createdBy;

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}
