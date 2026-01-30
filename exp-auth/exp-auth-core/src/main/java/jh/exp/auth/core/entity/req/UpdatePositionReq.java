package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新岗位请求对象
 */
@Data
public class UpdatePositionReq {

    /**
     * 岗位ID
     */
    @NotNull(message = "岗位ID不能为空")
    private Long postId;

    /**
     * 岗位编码
     */
    @NotBlank(message = "岗位编码不能为空")
    private String postCode;

    /**
     * 岗位名称
     */
    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    /**
     * 上级组织编号
     */
    private String orgCode;

    /**
     * 是否为外派岗位（0外派/1主岗位）
     */
    private Integer isOutsourcing;

    /**
     * 岗位类型
     */
    private String postType;

    /**
     * 岗位级别
     */
    private String postLevel;

    /**
     * 岗位类别
     */
    private String postCategory;

    /**
     * 岗位职责说明
     */
    private String postDesc;

    /**
     * 默认角色ID
     */
    private Long defaultRoleId;

    /**
     * 默认数据权限范围
     */
    private String defaultDataScope;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;
}
