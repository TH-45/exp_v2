package jh.exp.bid.contract.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 招标主表，对应 exp_tender
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exp_tender")
@TableName("exp_tender")
public class Tender {
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(name = "tender_id")
    private Long tenderId;

    // 招标编号
    @Column(name = "tender_code", length = 100)
    private String tenderCode;

    // 招标项目名称
    @Column(name = "tender_name", length = 200)
    private String tenderName;

    // 招标类型（工程、服务、货物等）
    @Column(name = "tender_type", length = 50)
    private String tenderType;

    // 招标方式（公开招标、邀请招标、竞争性谈判等）
    @Column(name = "tender_mode", length = 50)
    private String tenderMode;

    // 招标人/采购方ID，关联企业信息（内部单位或外部单位）
    @Column(name = "purchaser_id")
    private Long purchaserId;

    // 招标控制价/预算金额
    @Column(name = "budget_amount", precision = 20, scale = 2)
    private BigDecimal budgetAmount;

    // 币种
    @Column(name = "currency", length = 10)
    private String currency;

    // 招标项目概要/公告摘要
    @Column(name = "tender_brief", columnDefinition = "TEXT")
    private String tenderBrief;

    // 招标公告发布时间
    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    // 投标开始时间
    @Column(name = "bid_start_time")
    private LocalDateTime bidStartTime;

    // 投标截止时间
    @Column(name = "bid_end_time")
    private LocalDateTime bidEndTime;

    // 开标时间
    @Column(name = "open_time")
    private LocalDateTime openTime;

    // 开标地点或开标会议方式（线上/线下）
    @Column(name = "open_address", length = 500)
    private String openAddress;

    // 招标状态（准备、公告发布、投标中、开标中、评标中、已结束、已废标等）
    @Column(name = "status", length = 32)
    private String status;

    // 工程项目ID，关联工程项目服务模块（如已立项）
    @Column(name = "project_id")
    private Long projectId;

    // 创建人用户ID，关联账号信息表
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
