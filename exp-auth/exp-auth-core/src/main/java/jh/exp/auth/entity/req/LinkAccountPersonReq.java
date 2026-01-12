package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关联账号与人员请求
 */
@Data
public class LinkAccountPersonReq {

    /**
     * 账号ID
     */
    @NotNull(message = "账号ID不能为空")
    private Long accountId;

    /**
     * 人员ID
     */
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 账号名称
     */
    @NotNull(message = "账号名称不能为空")
    private String accountName;
}