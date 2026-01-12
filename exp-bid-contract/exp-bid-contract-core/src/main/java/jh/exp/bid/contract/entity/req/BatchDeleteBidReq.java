package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除投标请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDeleteBidReq {

    /**
     * 投标ID列表
     */
    @NotEmpty(message = "投标ID列表不能为空")
    private List<Long> bidIds;
}