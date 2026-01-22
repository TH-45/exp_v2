package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除角色请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDeleteRoleReq {

    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;

}