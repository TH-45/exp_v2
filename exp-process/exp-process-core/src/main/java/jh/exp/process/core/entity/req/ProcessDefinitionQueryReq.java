package jh.exp.process.core.entity.req;

import lombok.Data;

@Data
public class ProcessDefinitionQueryReq {
    private String procCode;
    private String procName;
    private String busType;
    private Integer isActive;
}
