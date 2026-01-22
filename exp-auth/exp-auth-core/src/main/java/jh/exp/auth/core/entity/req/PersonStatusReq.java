package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 人员状态变更请求对象
 */
@Data
public class PersonStatusReq {

    /**
     * 人员ID
     */
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
