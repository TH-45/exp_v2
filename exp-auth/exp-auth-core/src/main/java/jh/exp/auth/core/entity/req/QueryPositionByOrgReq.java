package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 根据组织ID查询岗位请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NotNull(message = "查询岗位请求对象不能为空")
public class QueryPositionByOrgReq {

    /**
     * 组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;


    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 岗位类型
     */
    private String postType;

    /**
     * 岗位状态
     */
    private String status;

    /**
     * 是否查询子项岗位
     */
    private Boolean includeChildren;
}
