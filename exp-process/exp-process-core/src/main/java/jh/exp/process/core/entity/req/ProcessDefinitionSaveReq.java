package jh.exp.process.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessDefinitionSaveReq {
    private Long procDefId;

    @NotBlank(message = "流程编码不能为空")
    private String procCode;

    @NotBlank(message = "流程名称不能为空")
    private String procName;

    @NotBlank(message = "业务类型不能为空")
    private String busType;

    private Integer isActive;
    private Integer version;
    private String remark;
}
