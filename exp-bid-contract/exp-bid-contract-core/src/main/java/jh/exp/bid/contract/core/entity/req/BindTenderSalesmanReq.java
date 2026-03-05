package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 绑定招标业务员请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindTenderSalesmanReq {

    /**
     * 招标ID
     */
    @NotNull(message = "招标ID不能为空")
    private Long tenderId;

    /**
     * 业务员ID
     */
    @NotNull(message = "业务员ID不能为空")
    private Long salesmanId;
}
