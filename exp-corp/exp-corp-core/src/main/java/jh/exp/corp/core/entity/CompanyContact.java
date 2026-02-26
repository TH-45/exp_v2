package jh.exp.corp.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业联系人信息表，对应 exp_company_contact
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_company_contact")
@TableName("exp_company_contact")
public class CompanyContact {

    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "contact_id")
    private Long contactId;

    // 所属企业ID
    @Column(name = "company_id")
    private Long companyId;

    // 联系人姓名
    @Column(name = "contact_name", length = 100)
    private String contactName;

    // 联系人职务/岗位
    @Column(name = "position", length = 100)
    private String position;

    // 手机号码
    @Column(name = "mobile", length = 32)
    private String mobile;

    // 固定电话
    @Column(name = "phone", length = 32)
    private String phone;

    // 邮箱
    @Column(name = "email", length = 100)
    private String email;

    // 微信号
    @Column(name = "wechat", length = 64)
    private String wechat;

    // 是否主要联系人（0/1）
    @Column(name = "is_primary")
    private Integer isPrimary;

    // 状态（ENABLED/DISABLED）
    @Column(name = "status", length = 32)
    private String status;

    // 备注
    @Column(name = "remark", length = 500)
    private String remark;
}
