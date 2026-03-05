package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建投标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBidReq {

    /**
     * 招标项目ID
     */
    @NotNull(message = "招标项目ID不能为空")
    private Long tenderId;

    /**
     * 投标单位ID
     */
    @NotNull(message = "投标单位ID不能为空")
    private Long supplierId;

    /**
     * 投标编号
     */
    @NotBlank(message = "投标编号不能为空")
    private String bidCode;

    /**
     * 投标名称
     */
    @NotBlank(message = "投标名称不能为空")
    private String bidName;

    /**
     * 投标总报价金额
     */
    @NotNull(message = "投标总报价金额不能为空")
    private BigDecimal bidTotalAmount;

    /**
     * 币种
     */
    @NotBlank(message = "币种不能为空")
    private String currency;

    /**
     * 负责人 ID
     */
    private Long  principalId;

    /**
     * 业务员 ID
     */
    private Long salesmanId;

    /**
     * 归属组织 ID
     */
    @NotNull(message = "归属组织ID不能为空")
    private Long orgId;


    /**
     * 投标提交时间
     */
    private LocalDateTime bidSubmitTime;

    /**
     * 工程项目ID
     */
    private Long projectId;

    /**
     * 备注
     */
    private String remark;
}