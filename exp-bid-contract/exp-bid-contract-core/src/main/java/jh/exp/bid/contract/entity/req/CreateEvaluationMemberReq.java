package jh.exp.bid.contract.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评标成员请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEvaluationMemberReq {

    /**
     * 评标委员会ID
     */
    @NotNull(message = "评标委员会ID不能为空")
    private Long committeeId;

    /**
     * 评标专家用户ID
     */
    @NotNull(message = "评标专家用户ID不能为空")
    private Long expertUserId;

    /**
     * 评标专家类型
     */
    private String expertType;

    /**
     * 在评标委员会中的角色
     */
    private String committeeRole;

    /**
     * 是否为主任评委（0/1）
     */
    private Integer isChairman;

    /**
     * 专家职称
     */
    private String expertTitle;

    /**
     * 专家专业领域
     */
    private String expertField;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 备注
     */
    private String remark;
}