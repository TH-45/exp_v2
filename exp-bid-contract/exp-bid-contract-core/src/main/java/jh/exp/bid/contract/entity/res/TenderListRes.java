package jh.exp.bid.contract.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 招标列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenderListRes {

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
     * 投标开始时间
     */
    private LocalDateTime bidStartTime;

    /**
     * 投标截止时间
     */
    private LocalDateTime bidEndTime;

    /**
     * 招标状态（准备、公告发布、投标中、开标中、评标中、已结束、已废标等）
     */
    private String status;

    /**
     * 工程项目名称
     */
    private String projectName;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}