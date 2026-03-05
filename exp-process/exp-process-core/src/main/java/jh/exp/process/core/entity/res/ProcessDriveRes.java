package jh.exp.process.core.entity.res;

import lombok.Data;

@Data
public class ProcessDriveRes {
    private String action;
    private Long instanceId;
    private Long taskId;
    private String busType;
    private String procCode;
    private String status;
    private String message;
}
