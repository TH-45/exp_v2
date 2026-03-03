package jh.exp.bid.contract.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询招标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryTenderReq {

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
     * 招标状态（准备、公告发布、投标中、开标中、评标中、已结束、已废标等）
     */
    private String status;

    /**
     * 工程项目ID
     */
    private Long projectId;

    /**
     * 创建人用户ID
     */
    private Long createdBy;

    /**
     * 投标开始时间开始范围
     */
    private LocalDateTime bidStartTimeStart;

    /**
     * 投标开始时间结束范围
     */
    private LocalDateTime bidStartTimeEnd;

    /**
     * 投标截止时间开始范围
     */
    private LocalDateTime bidEndTimeStart;

    /**
     * 投标截止时间结束范围
     */
    private LocalDateTime bidEndTimeEnd;

    /**
     * 是否含税（true 含税，false 不含税）
     */
    private Boolean isTaxIncluded;

    /**
     * 采购性质（1 政府采购 2 企业采购 3 其他）
     */
    private String purchaseNature;
}