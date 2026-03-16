package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建合同请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateContractReq {

    @NotBlank(message = "合同编号不能为空")
    private String contractCode;

    @NotBlank(message = "合同名称不能为空")
    private String contractName;

    /** 合同类型 */
    private String contractType;

    /** 合同类别 */
    private String contractCategory;

    /** 招标ID */
    private Long tenderId;

    /** 投标ID（中标记录） */
    private Long bidId;

    /** 工程项目ID */
    private Long projectId;

    /** 甲方单位ID */
    private Long purchaserId;

    @NotNull(message = "乙方/供应商不能为空")
    private Long supplierId;

    /** 合同签署日期 */
    private LocalDate signDate;

    /** 合同生效日期 */
    private LocalDate effectiveDate;

    /** 合同结束日期 */
    private LocalDate endDate;

    @NotNull(message = "合同金额不能为空")
    private BigDecimal amountTotal;

    /** 合同金额（不含税） */
    private BigDecimal amountWithoutTax;

    /** 默认税率 */
    private BigDecimal taxRateDefault;

    /** 币种 */
    private String currency;

    /** 付款条件 */
    private String payTerms;

    /** 结算方式 */
    private String settleMode;

    /** 备注 */
    private String remark;

    /** 业务员人员ID，通过人员选择器选择 */
    private Long salesmanPersonId;

    /** 动作*/
    @NotBlank(message = "动作不能为空")
    private String action;
}
