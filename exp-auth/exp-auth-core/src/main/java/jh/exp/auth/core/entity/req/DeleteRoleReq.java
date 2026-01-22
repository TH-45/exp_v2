package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除角色请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteRoleReq {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

}