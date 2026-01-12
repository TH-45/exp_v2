package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 投标状态请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidStatusReq {

    /**
     * 投标ID
     */
    @NotNull(message = "投标ID不能为空")
    private Long bidId;

    /**
     * 投标状态
     */
    @NotBlank(message = "投标状态不能为空")
    private String bidStatus;
}