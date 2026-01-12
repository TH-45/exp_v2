package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新投标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBidReq {

    /**
     * 投标ID
     */
    @NotNull(message = "投标ID不能为空")
    private Long bidId;

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
     * 投标提交时间
     */
    private LocalDateTime bidSubmitTime;

    /**
     * 备注
     */
    private String remark;
}