package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteByIdsReq {
    @NotEmpty(message = "ids不能为空")
    private List<Long> ids;
}
