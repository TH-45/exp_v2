package jh.exp.process.core.entity.res;

import lombok.Data;

import java.util.List;

@Data
public class ProcessDefinitionDetailRes {
    private Long procDefId;
    private String procCode;
    private String procName;
    private String busType;
    private Integer isActive;
    private Integer version;
    private String remark;
    private List<NodeRes> nodes;
}
