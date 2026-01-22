package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 组织状态变更请求对象
 */
@Data
public class OrgUnitStatusReq {

    /**
     * 组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}