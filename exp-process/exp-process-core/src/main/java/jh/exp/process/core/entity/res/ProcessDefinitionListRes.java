package jh.exp.process.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcessDefinitionListRes {
    private Long procDefId;
    private String procCode;
    private String procName;
    private String busType;
    private Integer isActive;
    private Integer version;
    private String remark;
    private LocalDateTime updatedTime;
}
