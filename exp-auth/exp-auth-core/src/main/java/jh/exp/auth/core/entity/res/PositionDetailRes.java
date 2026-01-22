package jh.exp.auth.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位详情响应对象
 */
@Data
public class PositionDetailRes {

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
     * 岗位职责说明
     */
    private String postDesc;

    /**
     * 岗位状态
     */
    private String status;

    /**
     * 默认角色ID
     */
    private Long defaultRoleId;

    /**
     * 默认角色名称
     */
    private String defaultRoleName;

    /**
     * 默认数据权限范围
     */
    private String defaultDataScope;

    /**
     * 是否系统内置
     */
    private Integer isSystem;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 备注
     */
    private String remark;
}
