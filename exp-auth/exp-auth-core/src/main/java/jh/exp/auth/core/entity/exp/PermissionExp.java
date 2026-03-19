package jh.exp.auth.core.entity.exp;

import jh.exp.auth.core.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionExp extends Permission {
    /** 角色ID */
    private Long roleId;
    /** 授权类型：MENU 时为 1/2/3，FUNC 时可为空 */
    private String grantType;
}
