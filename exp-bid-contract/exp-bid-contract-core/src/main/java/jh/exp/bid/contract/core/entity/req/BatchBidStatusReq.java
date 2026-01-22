package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量投标状态请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchBidStatusReq {

    /**
     * 投标ID列表
     */
    @NotEmpty(message = "投标ID列表不能为空")
    private List<Long> bidIds;

    /**
     * 投标状态
     */
    @NotBlank(message = "投标状态不能为空")
    private String bidStatus;
}