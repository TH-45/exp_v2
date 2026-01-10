package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色状态变更请求对象
 */
@Data
public class RoleStatusReq {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}