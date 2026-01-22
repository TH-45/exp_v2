package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除组织请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDeleteOrgUnitReq {

    /**
     * 组织ID列表
     */
    @NotEmpty(message = "组织ID列表不能为空")
    private List<Long> orgIds;
}