package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除招标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteTenderReq {

    /**
     * 招标ID
     */
    @NotNull(message = "招标ID不能为空")
    private Long tenderId;
}