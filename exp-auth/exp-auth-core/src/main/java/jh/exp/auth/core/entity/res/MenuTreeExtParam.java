package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单树扩展参数
 *
 * @param <T> 扩展字段类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuTreeExtParam<T> {

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 扩展字段
     */
    private T ext;
}
