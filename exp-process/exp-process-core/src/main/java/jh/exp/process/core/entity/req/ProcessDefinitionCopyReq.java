package jh.exp.process.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessDefinitionCopyReq {
    @NotNull(message = "源流程ID不能为空")
    private Long sourceProcDefId;

    @NotBlank(message = "新流程编码不能为空")
    private String newProcCode;

    @NotBlank(message = "新流程名称不能为空")
    private String newProcName;
}
