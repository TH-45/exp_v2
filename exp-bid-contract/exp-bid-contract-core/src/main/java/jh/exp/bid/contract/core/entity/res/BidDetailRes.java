package jh.exp.bid.contract.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标详情响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidDetailRes {

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
     * 合同ID
     */
    private Long contractId;

    /**
     * 工程项目ID
     */
    private Long projectId;

    /**
     * 工程项目名称
     */
    private String projectName;

    /**
     * 创建人ID
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