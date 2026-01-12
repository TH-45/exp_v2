package jh.exp.bid.contract.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询评标委员会请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryEvaluationCommitteeReq {

    /**
     * 招标项目ID
     */
    private Long tenderId;

    /**
     * 委员会编号
     */
    private String committeeCode;

    /**
     * 委员会名称
     */
    private String committeeName;

    /**
     * 评标方式
     */
    private String evaluationMethod;

    /**
     * 委员会状态
     */
    private String status;

    /**
     * 评标负责人ID
     */
    private Long evaluationDirectorId;

    /**
     * 评标开始时间开始
     */
    private LocalDateTime evaluationStartTimeStart;

    /**
     * 评标开始时间结束
     */
    private LocalDateTime evaluationStartTimeEnd;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间开始
     */
    private LocalDateTime createdTimeStart;

    /**
     * 创建时间结束
     */
    private LocalDateTime createdTimeEnd;
}