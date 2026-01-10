package jh.exp.auth.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色查询请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryRoleReq {

    private String roleCode;
    private String roleName;
    private String status;
    private String roleType;

}