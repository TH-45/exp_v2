package jh.exp.bid.contract.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合同查询请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryContractReq {

    /** 合同编号（模糊） */
    private String contractCode;

    /** 合同名称（模糊） */
    private String contractName;

    /** 关联项目ID */
    private Long projectId;

    /** 关联项目ID列表（按项目名称解析后传入） */
    private java.util.List<Long> projectIds;

    /** 关联项目名称（模糊，Service 解析为 projectIds） */
    private String projectName;

    /** 金额下限（万元） */
    private BigDecimal amountMin;

    /** 金额上限（万元） */
    private BigDecimal amountMax;

    /** 签订日期起 */
    private LocalDate signDateStart;

    /** 签订日期止 */
    private LocalDate signDateEnd;

    /** 生效日期起 */
    private LocalDate effectiveDateStart;

    /** 生效日期止 */
    private LocalDate effectiveDateEnd;

    /** 合同状态 */
    private String status;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商ID列表（按供应商名称解析后传入） */
    private java.util.List<Long> supplierIds;

    /** 供应商名称（模糊，Service 解析为 supplierIds） */
    private String supplierName;

    /**
     * 合作方类型：1-甲方(purchaser_id)，2-供应商(supplier_id)，空-两者皆可
     * 字典 Partner_Type
     */
    private String partnerType;

    /** 合作方名称（模糊，Service 按 partnerType 解析为 purchaserIds/supplierIds/partnerIds） */
    private String partnerName;

    /** 甲方ID列表（partnerType=1 时由 partnerName 解析） */
    private java.util.List<Long> purchaserIds;

    /** 合作方ID列表（partnerType 为空时，purchaser_id 或 supplier_id 任一匹配即可） */
    private java.util.List<Long> partnerIds;
}
