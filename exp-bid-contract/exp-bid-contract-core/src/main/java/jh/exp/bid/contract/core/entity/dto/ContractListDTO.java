package jh.exp.bid.contract.core.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同列表 DTO（供 Mapper 查询结果映射）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractListDTO {

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
    private String currency;
    private String status;
    private LocalDateTime createdTime;
}
