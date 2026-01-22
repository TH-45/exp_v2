package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除组织请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteOrgUnitReq {

    /**
     * 组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;
}