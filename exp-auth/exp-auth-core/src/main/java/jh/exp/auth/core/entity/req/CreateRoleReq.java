package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建角色请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoleReq {

    /**
     * 角色编码（唯一）
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色类型（ADMIN-管理员角色，BUSINESS-业务角色，SYSTEM-系统角色）
     */
    @NotBlank(message = "角色类型不能为空")
    private String roleType;

    /**
     * 数据权限范围类型（ALL-全部数据，DEPT-部门数据，SELF-个人数据，CUSTOM-自定义）
     */
    private String dataScopeType;

    /**
     * 数据权限范围详情（JSON格式）
     */
    private String dataScopeDetail;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;

}