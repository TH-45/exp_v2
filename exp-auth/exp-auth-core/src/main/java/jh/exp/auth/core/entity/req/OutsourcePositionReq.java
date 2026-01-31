package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外派岗位请求对象
 */
@Data
public class OutsourcePositionReq {

    /**
     * 岗位ID
     */
    @NotNull(message = "岗位ID不能为空")
    private Long postId;

    /**
     * 当前组织ID
     */
    @NotNull(message = "当前组织不能为空")
    private Long currentOrgId;

    /**
     * 目标外派组织ID
     */
    @NotNull(message = "外派组织不能为空")
    private Long targetOrgId;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;

    /**
     * 是否为外派岗位（1外派/0非外派）
     */
    @NotNull(message = "是否外派不能为空")
    private Integer isOutsourcing;

    /**
     * 备注
     */
    private String remark;
}
