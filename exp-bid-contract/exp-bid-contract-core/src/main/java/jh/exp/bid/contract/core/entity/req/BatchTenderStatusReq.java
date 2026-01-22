package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量招标状态变更请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchTenderStatusReq {

    /**
     * 招标ID列表
     */
    @NotEmpty(message = "招标ID列表不能为空")
    private List<Long> tenderIds;

    /**
     * 招标状态（准备、公告发布、投标中、开标中、评标中、已结束、已废标等）
     */
    @NotBlank(message = "招标状态不能为空")
    private String status;
}