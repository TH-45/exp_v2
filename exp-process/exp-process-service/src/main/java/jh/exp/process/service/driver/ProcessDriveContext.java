package jh.exp.process.service.driver;

import jh.exp.process.core.entity.req.ProcessDriveReq;
import lombok.Data;

@Data
public class ProcessDriveContext {
    private ProcessDriveReq req;
    private String action;
    private String procCode;
    private String busType;
    private String busId;
    private Long taskId;
    private Long instanceId;
    private Long operatorId;
    private String instanceStatus;
}
