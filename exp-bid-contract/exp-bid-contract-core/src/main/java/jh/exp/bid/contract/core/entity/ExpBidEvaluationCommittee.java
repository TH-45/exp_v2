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
 * 评标委员会表，对应 exp_bid_evaluation_committee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_bid_evaluation_committee")
@TableName("exp_bid_evaluation_committee")
public class ExpBidEvaluationCommittee {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "committee_id")
    private Long committeeId;

    // 招标项目ID，关联 exp_tender
    @Column(name = "tender_id")
    private Long tenderId;

    // 委员会编号
    @Column(name = "committee_code", length = 100)
    private String committeeCode;

    // 委员会名称
    @Column(name = "committee_name", length = 200)
    private String committeeName;

    // 评标方式（综合评标、资格后审等）
    @Column(name = "evaluation_method", length = 50)
    private String evaluationMethod;

    // 评标地点
    @Column(name = "evaluation_location", length = 200)
    private String evaluationLocation;

    // 评标开始时间
    @Column(name = "evaluation_start_time")
    private LocalDateTime evaluationStartTime;

    // 评标结束时间
    @Column(name = "evaluation_end_time")
    private LocalDateTime evaluationEndTime;

    // 委员会状态（组建中、已组建、评标中、已完成）
    @Column(name = "status", length = 32)
    private String status;

    // 评标负责人ID，关联账号信息
    @Column(name = "evaluation_director_id")
    private Long evaluationDirectorId;

    // 监督人ID，关联账号信息
    @Column(name = "supervisor_id")
    private Long supervisorId;

    // 创建人用户ID，关联账号信息
    @Column(name = "created_by")
    private Long createdBy;

    // 创建人部门ID，关联部门管理
    @Column(name = "created_dept_id")
    private Long createdDeptId;

    // 创建人岗位ID，关联岗位管理
    @Column(name = "created_post_id")
    private Long createdPostId;

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