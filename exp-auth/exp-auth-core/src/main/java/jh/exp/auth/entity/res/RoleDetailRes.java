package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色详情响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDetailRes {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型
     */
    private String roleType;

    /**
     * 状态
     */
    private String status;

    /**
     * 数据权限范围类型
     */
    private String dataScopeType;

    /**
     * 数据权限范围详情
     */
    private String dataScopeDetail;

    /**
     * 系统内置标识
     */
    private Integer isSystem;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 创建人
     */
    private String createdName;

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