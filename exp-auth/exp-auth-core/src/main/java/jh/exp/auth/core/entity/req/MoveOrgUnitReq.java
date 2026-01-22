package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动组织请求（更改组织树结构）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveOrgUnitReq {

    /**
     * 要移动的组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    /**
     * 目标父组织ID（根节点为0或NULL）
     */
    private Long targetParentOrgId;

    /**
     * 排序号
     */
    private Integer sortNo;
}