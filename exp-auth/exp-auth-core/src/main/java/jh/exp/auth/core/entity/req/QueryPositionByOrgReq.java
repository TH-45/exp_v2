package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 根据组织ID查询岗位请求对象
 */
@Data
public class QueryPositionByOrgReq {

    /**
     * 组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    /**
     * 岗位状态（可选，用于过滤）
     */
    private String status;

    /**
     * 是否查询子项岗位
     */
    private Boolean includeChildren;

}
