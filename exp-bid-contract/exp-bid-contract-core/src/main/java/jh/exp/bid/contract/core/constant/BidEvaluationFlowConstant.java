package jh.exp.bid.contract.core.constant;

/**
 * 评标/定标流程常量
 */
public class BidEvaluationFlowConstant {

    private BidEvaluationFlowConstant() {
    }

    /**
     * 允许进入评标/定标流程的公司名称（trim 后精确匹配）
     */
    public static final String FLOW_COMPANY_NAME = "广州江浩科技有限公司";

    /**
     * 允许进入评标/定标流程的公司类型（忽略大小写）
     */
    public static final String FLOW_COMPANY_TYPE_SELF = "SELF";

    /**
     * 重招标记
     */
    public static final String FLOW_RETENDER_FLAG = "RETENDER";

    /**
     * 驳回后默认回退到评标结果已确认
     */
    public static final String REJECT_BACK_TO_RESULT_CONFIRMED = "RESULT_CONFIRMED";

    /**
     * 驳回后条件回退到评分中
     */
    public static final String REJECT_BACK_TO_SCORING = "SCORING";

    /**
     * 驳回原因：需要重新评分/复评
     */
    public static final String REJECT_REASON_SCORE_REWORK = "SCORE_REWORK";

    /**
     * 驳回原因：文档补正
     */
    public static final String REJECT_REASON_DOC_FIX = "DOC_FIX";
}
