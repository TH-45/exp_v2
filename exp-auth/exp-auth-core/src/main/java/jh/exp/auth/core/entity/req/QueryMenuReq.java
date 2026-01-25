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

    /**
     * 菜单编码
     */
    private String menuCode;
    /**
     * 菜单名称
     */
    private String menuName;
    /**
     * 菜单类型
     */
    private String menuType;
    /**
     * 状态
     */
    private String status;

}