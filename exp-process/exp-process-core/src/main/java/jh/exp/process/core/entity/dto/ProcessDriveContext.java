package jh.exp.process.core.entity.dto;

import jh.exp.process.core.entity.req.ProcessDriveReq;
import lombok.Data;

@Data
public class ProcessDriveContext {
    /** 流程驱动请求对象 */
    private ProcessDriveReq req;
    /** 操作动作 */
    private String action;
    /** 流程编码 */
    private String procCode;
    /** 业务类型 */
    private String busType;
    /** 业务 ID */
    private Long busId;
    /** 任务 ID */
    private Long taskId;
    /** 流程实例 ID */
    private Long instanceId;
    /** 操作人 ID */
    private Long operatorId;
    /** 实例状态 */
    private String instanceStatus;

    /** 业务数据 */
    private Object businessData;
}
