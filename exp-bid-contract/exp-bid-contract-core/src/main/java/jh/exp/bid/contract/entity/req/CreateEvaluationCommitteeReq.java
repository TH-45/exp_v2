package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建评标委员会请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEvaluationCommitteeReq {

    /**
     * 招标项目ID
     */
    @NotNull(message = "招标项目ID不能为空")
    private Long tenderId;

    /**
     * 委员会编号
     */
    @NotBlank(message = "委员会编号不能为空")
    private String committeeCode;

    /**
     * 委员会名称
     */
    @NotBlank(message = "委员会名称不能为空")
    private String committeeName;

    /**
     * 评标方式
     */
    @NotBlank(message = "评标方式不能为空")
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
     * 评标负责人ID
     */
    private Long evaluationDirectorId;

    /**
     * 监督人ID
     */
    private Long supervisorId;

    /**
     * 备注
     */
    private String remark;
}