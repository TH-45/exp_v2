package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteCompanyContactReq {
    @NotEmpty(message = "contactIds不能为空")
    private List<Long> contactIds;
}
