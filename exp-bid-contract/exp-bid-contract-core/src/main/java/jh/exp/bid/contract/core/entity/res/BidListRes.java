package jh.exp.bid.contract.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidListRes {

    /**
     * 投标ID
     */
    private Long bidId;

    /**
     * 招标项目ID
     */
    private Long tenderId;

    /**
     * 招标项目名称
     */
    private String tenderName;

    /**
     * 投标单位ID
     */
    private Long supplierId;

    /**
     * 投标单位名称
     */
    private String supplierName;

    /**
     * 投标编号
     */
    private String bidCode;

    /**
     * 投标名称
     */
    private String bidName;

    /**
     * 投标总报价金额
     */
    private BigDecimal bidTotalAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 投标提交时间
     */
    private LocalDateTime bidSubmitTime;

    /**
     * 投标状态
     */
    private String bidStatus;

    /**
     * 是否中标
     */
    private Integer winFlag;

    /**
     * 中标通知书编号
     */
    private String winNoticeNo;

    /**
     * 工程项目ID（用于服务层调用项目模块补全 projectName）
     */
    private Long projectId;

    /**
     * 工程项目名称
     */
    private String projectName;

    /**
     * 创建人ID（用于服务层调用 auth 模块补全 createdByName）
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

//    ------------------------------------------------

    /**
     * 负责人 ID
     */
    private Long managerPersonId;
    /**
     * 负责人姓名
     */
    private String managerPersonName;

    /**
     * 归属组织id
     */
    private Long orgId;
    /**
     * 归属组织名称
     */
    private String orgIdName;
    /**
     * 业务员ID
     */
    private Long salesmanId;
    /**
     * 业务员姓名
     */
    private String salesmanName;


}