package jh.exp.auth.core.entity.res;

import lombok.Data;

/**
 * 岗位列表响应对象
 */
@Data
public class PositionListRes {

    /**
     * 岗位ID
     */
    private Long postId;

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
     * 岗位级别
     */
    private String postLevel;

    /**
     * 岗位类别
     */
    private String postCategory;

    /**
     * 岗位状态
     */
    private String status;

    /**
     * 默认角色名称
     */
    private String defaultRoleName;

    /**
     * 默认角色ID
     */
    private Long defaultRoleId;

    /**
     * 是否为外派岗位（1外派/0非外派）
     */
    private Integer isOutsourcing;

    /**
     * 是否系统内置
     */
    private Integer isSystem;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 创建时间
     */
    private String createdTime;
}
