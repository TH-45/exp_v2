package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleListRes {

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
     * 备注
     */
    private String remark;

}