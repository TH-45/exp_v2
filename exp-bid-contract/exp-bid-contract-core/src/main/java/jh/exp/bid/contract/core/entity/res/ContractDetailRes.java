package jh.exp.bid.contract.core.entity.res;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同详情响应
 */
@Data
public class ContractDetailRes {

    private Long contractId;
    private String contractCode;
    private String contractName;
    private String contractType;
    private String contractCategory;
    private Long tenderId;
    private Long bidId;
    private Long projectId;
    private String projectName;
    private Long purchaserId;
    private String purchaserName;
    private Long supplierId;
    private String supplierName;
    private LocalDate signDate;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private BigDecimal amountTotal;
    private BigDecimal amountWithoutTax;
    private BigDecimal taxRateDefault;
    private String currency;
    private String payTerms;
    private String settleMode;
    private String status;
    private Integer archiveFlag;
    private LocalDateTime archiveTime;
    private Long signUserId;
    private LocalDateTime signTime;
    private Long createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String remark;

    /** 提单人：人员名称、岗位、手机号（当前登录人，不可修改） */
    private String creatorName;
    private String creatorPostName;
    private String creatorMobile;

    /** 业务员：人员名称、岗位、手机号 */
    private Long salesmanPersonId;
    private String salesmanName;
    private String salesmanPostName;
    private String salesmanMobile;

//    /** 流程实例ID，仅当创建时 action=SUBMIT 时有值 */
//    private Long instanceId;
}
