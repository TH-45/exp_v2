package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量角色状态变更请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchRoleStatusReq {

    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;

    /**
     * 状态（ENABLED-启用，DISABLED-停用）
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}