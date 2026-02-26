package jh.exp.corp.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业资质信息表，对应 exp_qualification
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_qualification")
@TableName("exp_qualification")
public class Qualification {

    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "qualification_id")
    private Long qualificationId;

    // 所属企业ID
    @Column(name = "company_id")
    private Long companyId;

    // 资质证书编号
    @Column(name = "qualification_code", length = 100)
    private String qualificationCode;

    // 资质名称
    @Column(name = "qualification_name", length = 200)
    private String qualificationName;

    // 资质类型/类别
    @Column(name = "qualification_type", length = 64)
    private String qualificationType;

    // 发证机关名称
    @Column(name = "issue_org", length = 200)
    private String issueOrg;

    // 发证日期
    @Column(name = "issue_date")
    private LocalDate issueDate;

    // 有效期起始日期
    @Column(name = "valid_from")
    private LocalDate validFrom;

    // 有效期截止日期
    @Column(name = "valid_to")
    private LocalDate validTo;

    // 过期提前预警天数
    @Column(name = "warn_days")
    private Integer warnDays;

    // 状态（VALID/EXPIRED/WILL_EXPIRE/DISABLED）
    @Column(name = "status", length = 32)
    private String status;

    // 纸质档案存放编号
    @Column(name = "file_no", length = 100)
    private String fileNo;

    // 是否有扫描件/附件（0/1）
    @Column(name = "attach_flag")
    private Integer attachFlag;

    // 创建人用户ID
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
