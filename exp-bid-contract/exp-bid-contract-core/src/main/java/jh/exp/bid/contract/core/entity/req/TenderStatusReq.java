package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 招标状态变更请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenderStatusReq {

    /**
     * 招标ID
     */
    @NotNull(message = "招标ID不能为空")
    private Long tenderId;

    /**
     * 招标状态（准备、公告发布、投标中、开标中、评标中、已结束、已废标等）
     */
    @NotBlank(message = "招标状态不能为空")
    private String status;
}