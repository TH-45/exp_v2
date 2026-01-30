package jh.exp.auth.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员基础信息表，对应 exp_person
 * 用于存储企业/组织内所有人员的基本信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_person")
@TableName("exp_person")
public class Person {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "person_id")
    private Long personId;

    // 人员工号/编号
    @Column(name = "person_code", nullable = false, unique = true, length = 50)
    private String personCode;

    // 姓名
    @Column(name = "person_name", nullable = false, length = 100)
    private String personName;

    // 性别（M/F/OTHER 等，可选）
    @Column(name = "gender", length = 10)
    private String gender;

    // 手机号码
    @Column(name = "mobile", length = 20)
    private String mobile;

    // 邮箱
    @Column(name = "email", length = 100)
    private String email;

    // 身份证号/证件号（可选，视实际业务与隐私要求）
    @Column(name = "id_card_no", length = 30)
    private String idCardNo;

    // 职务（如"部门经理""工程师"等，用于展示）
    @Column(name = "job_title", length = 100)
    private String jobTitle;

    // 主属组织/部门ID，关联 exp_org_unit.org_id
    @Column(name = "org_id")
    private Long orgId;

    // 主岗位ID，关联 exp_post.post_id
    @Column(name = "post_id")
    private Long postId;

    // 绑定账号ID，关联账号信息表（1人1账号为主）
    @Column(name = "account_id")
    private Long accountId;

    // 人员状态（ONJOB-在职，LEAVE-离职，DISABLED-停用 等，联动账号状态）
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // 入职日期
    @Column(name = "entry_date")
    private LocalDate entryDate;

    // 离职日期（仅离职人员使用）
    @Column(name = "leave_date")
    private LocalDate leaveDate;

    // 是否外部人员/合作方（0/1）
    @Column(name = "is_external")
    private Integer isExternal;

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