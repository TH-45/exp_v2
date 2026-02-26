package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteCompanyContactReq {
    @NotNull(message = "contactId不能为空")
    private Long contactId;
}
