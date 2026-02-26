package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteCompanyReq {
    @NotEmpty(message = "companyIds不能为空")
    private List<Long> companyIds;
}
