package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量人员状态变更请求对象
 */
@Data
public class BatchPersonStatusReq {

    /**
     * 要变更状态的人员ID列表
     */
    @NotEmpty(message = "人员ID列表不能为空")
    private List<Long> personIds;

    /**
     * 目标状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
