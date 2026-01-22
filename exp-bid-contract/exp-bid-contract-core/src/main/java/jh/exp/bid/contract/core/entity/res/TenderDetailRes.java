package jh.exp.bid.contract.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 招标详情响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenderDetailRes {

    /**
     * 招标ID
     */
    private Long tenderId;

    /**
     * 招标编号
     */
    private String tenderCode;

    /**
     * 招标项目名称
     */
    private String tenderName;

    /**
     * 招标类型（工程、服务、货物等）
     */
    private String tenderType;

    /**
     * 招标方式（公开招标、邀请招标、竞争性谈判等）
     */
    private String tenderMode;

    /**
     * 招标人/采购方ID
     */
    private Long purchaserId;

    /**
     * 招标人/采购方名称
     */
    private String purchaserName;

    /**
     * 招标控制价/预算金额
     */
    private BigDecimal budgetAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 招标项目概要/公告摘要
     */
    private String tenderBrief;

    /**
     * 招标公告发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 投标开始时间
     */
    private LocalDateTime bidStartTime;

    /**
     * 投标截止时间
     */
    private LocalDateTime bidEndTime;

    /**
     * 开标时间
     */
    private LocalDateTime openTime;

    /**
     * 开标地点或开标会议方式（线上/线下）
     */
    private String openAddress;

    /**
     * 招标状态（准备、公告发布、投标中、开标中、评标中、已结束、已废标等）
     */
    private String status;

    /**
     * 工程项目ID
     */
    private Long projectId;

    /**
     * 工程项目名称
     */
    private String projectName;

    /**
     * 工程项目负责人ID
     */
    private Long projectManagerId;

    /**
     * 工程项目负责人姓名
     */
    private String projectManagerName;

    /**
     * 工程项目归属组织ID
     */
    private Long projectOrgId;

    /**
     * 工程项目归属组织名称
     */
    private String projectOrgName;

    /**
     * 组织负责人ID
     */
    private Long orgManagerId;

    /**
     * 组织负责人姓名
     */
    private String orgManagerName;

    /**
     * 创建人用户ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建人部门ID
     */
    private Long createdDeptId;

    /**
     * 创建人部门名称
     */
    private String createdDeptName;

    /**
     * 创建人岗位ID
     */
    private Long createdPostId;

    /**
     * 创建人岗位名称
     */
    private String createdPostName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 备注
     */
    private String remark;
}