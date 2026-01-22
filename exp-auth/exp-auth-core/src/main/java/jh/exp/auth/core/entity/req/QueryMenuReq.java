package jh.exp.auth.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单查询请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryMenuReq {

    private String menuCode;
    private String menuName;
    private String menuType;
    private String status;

}