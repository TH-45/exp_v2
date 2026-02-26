package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteCompanyReq {
    @NotNull(message = "companyId不能为空")
    private Long companyId;
}
