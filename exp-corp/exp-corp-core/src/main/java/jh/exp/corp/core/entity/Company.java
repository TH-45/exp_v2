package jh.exp.corp.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 企业基础信息表，对应 exp_company
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_company")
@TableName("exp_company")
public class Company {

    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "company_id")
    private Long companyId;

    // 企业编码（系统内部编码，可选）
    @Column(name = "company_code", length = 64)
    private String companyCode;

    // 企业全称
    @Column(name = "company_name", length = 200)
    private String companyName;

    // 企业简称
    @Column(name = "company_short_name", length = 100)
    private String companyShortName;

    // 企业类型（SELF/SUPPLIER/PARTNER/CLIENT等）
    @Column(name = "company_type", length = 32)
    private String companyType;

    // 统一社会信用代码
    @Column(name = "unified_social_credit_code", length = 64)
    private String unifiedSocialCreditCode;

    // 纳税人识别号
    @Column(name = "tax_no", length = 64)
    private String taxNo;

    // 法人代表
    @Column(name = "legal_person", length = 100)
    private String legalPerson;

    // 注册地址
    @Column(name = "reg_address", length = 500)
    private String regAddress;

    // 办公地址
    @Column(name = "office_address", length = 500)
    private String officeAddress;

    // 企业联系电话
    @Column(name = "contact_phone", length = 64)
    private String contactPhone;

    // 企业联系邮箱
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    // 官网地址
    @Column(name = "website", length = 255)
    private String website;

    // 状态（ENABLED/DISABLED/BLACKLIST等）
    @Column(name = "status", length = 32)
    private String status;

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
