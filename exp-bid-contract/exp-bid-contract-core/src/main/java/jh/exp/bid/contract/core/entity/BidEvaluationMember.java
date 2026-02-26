package jh.exp.bid.contract.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评标委员会成员表，对应 exp_bid_evaluation_member
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_evaluation_member")
@TableName("exp_bid_evaluation_member")
public class BidEvaluationMember {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "member_id")
    private Long memberId;

    // 评标委员会ID，关联 exp_bid_evaluation_committee
    @Column(name = "committee_id")
    private Long committeeId;

    // 评标专家用户ID，关联账号信息
    @Column(name = "expert_user_id")
    private Long expertUserId;

    // 评标专家类型（技术专家、商务专家、经济专家等）
    @Column(name = "expert_type", length = 50)
    private String expertType;

    // 在评标委员会中的角色（组长、副组长、成员）
    @Column(name = "committee_role", length = 32)
    private String committeeRole;

    // 是否为主任评委（0/1）
    @Column(name = "is_chairman")
    private Integer isChairman;

    // 专家职称
    @Column(name = "expert_title", length = 100)
    private String expertTitle;

    // 专家专业领域
    @Column(name = "expert_field", length = 200)
    private String expertField;

    // 联系电话
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    // 联系邮箱
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    // 是否到场（0/1）
    @Column(name = "is_present")
    private Integer isPresent;

    // 成员状态（待确认、已确认、已拒绝、已到场、已离场）
    @Column(name = "member_status", length = 32)
    private String memberStatus;

    // 加入时间
    @Column(name = "join_time")
    private LocalDateTime joinTime;

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
