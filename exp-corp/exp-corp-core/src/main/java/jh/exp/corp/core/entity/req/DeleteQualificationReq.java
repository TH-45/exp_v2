package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteQualificationReq {
    @NotNull(message = "qualificationId不能为空")
    private Long qualificationId;
}
