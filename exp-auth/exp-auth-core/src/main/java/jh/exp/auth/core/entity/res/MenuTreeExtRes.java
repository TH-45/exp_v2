package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

/**
 * 带扩展字段的菜单树响应
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuTreeExtRes extends MenuTreeRes {

    /**
     * 扩展字段
     */
    private Long ext;
}
