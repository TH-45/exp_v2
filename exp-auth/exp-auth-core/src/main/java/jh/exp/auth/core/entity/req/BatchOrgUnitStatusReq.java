package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量组织状态变更请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchOrgUnitStatusReq {

    /**
     * 组织ID列表
     */
    @NotEmpty(message = "组织ID列表不能为空")
    private List<Long> orgIds;

    /**
     * 状态（ENABLED-启用，DISABLED-停用）
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
