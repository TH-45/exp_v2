package jh.exp.process.core.entity.req;

import lombok.Data;

@Data
public class ApprovalTaskQueryReq {
    /**
     * 标签页
     */
    private String tab;
    /**
     * 实例id（流程实例表 wf_instance.instance_id）
     */
    private String instanceId;
    /**
     * 实例标题
     */
    private String instanceTitle;
    /**
     * 业务类型
     */
    private String busType;
    /**
     * 状态
     */
    private String status;

    /**
     * 关键词（流程名/业务ID，前端传入，映射到 instanceTitle 模糊查询）
     */
    private String keyword;
}
