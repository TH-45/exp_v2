package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建岗位请求对象
 */
@Data
public class CreatePositionReq {
    /**
     * 上级组织编号
     */
    @NotBlank(message = "上级组织编号不能为空")
    private String orgCode;

    /**
     * 是否为外派岗位（1外派/0非外派）
     */
    @NotNull(message = "是否为外派岗位不能为空")
    private Integer isOutsourcing;

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
     * 岗位类型
     */
    @NotBlank(message = "岗位类型不能为空")
    private String postType;

    /**
     * 岗位级别
     */
    @NotBlank(message = "岗位级别不能为空")
    private String postLevel;

    /**
     * 岗位类别
     */
    @NotBlank(message = "岗位类别不能为空")
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
