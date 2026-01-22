package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除投标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteBidReq {

    /**
     * 投标ID
     */
    @NotNull(message = "投标ID不能为空")
    private Long bidId;
}