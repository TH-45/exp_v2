package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除菜单请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDeleteMenuReq {

    /**
     * 菜单ID列表
     */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> menuIds;

}