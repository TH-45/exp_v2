package jh.exp.bid.contract.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评标委员会列表响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationCommitteeListRes {

    /**
     * 委员会ID
     */
    private Long committeeId;

    /**
     * 招标项目ID
     */
    private Long tenderId;

    /**
     * 招标项目名称
     */
    private String tenderName;

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
     * 评标地点
     */
    private String evaluationLocation;

    /**
     * 评标开始时间
     */
    private LocalDateTime evaluationStartTime;

    /**
     * 评标结束时间
     */
    private LocalDateTime evaluationEndTime;

    /**
     * 委员会状态
     */
    private String status;

    /**
     * 评标负责人姓名
     */
    private String evaluationDirectorName;

    /**
     * 监督人姓名
     */
    private String supervisorName;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}