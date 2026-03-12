package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新合同请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateContractReq {

    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    private String contractCode;
    private String contractName;
    private String contractType;
    private String contractCategory;
    private Long tenderId;
    private Long bidId;
    private Long projectId;
    private Long purchaserId;
    private Long supplierId;
    private LocalDate signDate;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private BigDecimal amountTotal;
    private BigDecimal amountWithoutTax;
    private BigDecimal taxRateDefault;
    private String currency;
    private String payTerms;
    private String settleMode;
    private String remark;

    /** 业务员人员ID */
    private Long salesmanPersonId;
}
