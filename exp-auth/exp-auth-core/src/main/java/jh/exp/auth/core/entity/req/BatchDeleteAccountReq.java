package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除账号请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDeleteAccountReq {

    /**
     * 账号ID列表
     */
    @NotEmpty(message = "账号ID列表不能为空")
    private List<Long> accountIds;

    /**
     * 操作备注
     */
    private String remark;
}
