package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号角色响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRoleRes {

    /**
     * 账号ID
     */
    private Long accountId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;
}