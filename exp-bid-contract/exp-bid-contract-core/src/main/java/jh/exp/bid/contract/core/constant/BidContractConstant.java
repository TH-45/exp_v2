package jh.exp.bid.contract.core.constant;

public class BidContractConstant {

    /**
     * 负责人
     */
    public static final String BID_CONTRACT_PRINCIPAL = "PM";

    /**
     * 业务员
     */
    public static final String BID_CONTRACT_SALESMAN = "SP";

    /**
     * 招标项目准备状态
     */
    public static final String BID_CONTRACT_PROJECT_PREPARE = "PREPARE";

    /**
     * 已提交
     */
    public static final String BID_CONTRACT_PROJECT_SUBMITTED = "SUBMITTED";

    /**
     * 评审中
     */
    public static final String BID_CONTRACT_PROJECT_EVALUATING = "EVALUATING";

    /**
     * 中标
     */
    public static final String BID_CONTRACT_PROJECT_WON = "WON";

    /**
     * 未中标
     */
    public static final String BID_CONTRACT_PROJECT_LOST = "LOST";

    /**
     * 放弃
     */
    public static final String BID_CONTRACT_PROJECT_ABANDONED = "ABANDONED";

    // ========== 合同状态（与流程：起草→提交→审核→拟签→归档） ==========
    /** 起草中 */
    public static final String CONTRACT_STATUS_DRAFT = "DRAFT";
    /** 审核中 */
    public static final String CONTRACT_STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    /** 拟签 */
    public static final String CONTRACT_STATUS_PENDING_SIGN = "PENDING_SIGN";
    /** 履行中/生效 */
    public static final String CONTRACT_STATUS_EFFECTIVE = "EFFECTIVE";
    /** 正常归档 */
    public static final String CONTRACT_STATUS_ARCHIVED = "ARCHIVED";
    /** 异常归档 */
    public static final String CONTRACT_STATUS_ARCHIVED_ABNORMAL = "ARCHIVED_ABNORMAL";
    /** 已变更 */
    public static final String CONTRACT_STATUS_CHANGED = "CHANGED";
    /** 已终止 */
    public static final String CONTRACT_STATUS_TERMINATED = "TERMINATED";
    /** 已作废 */
    public static final String CONTRACT_STATUS_REJECTED = "REJECTED";

    // ========== 签订操作类型 ==========
    /** 签订 */
    public static final String SIGN_ACTION_SIGN = "SIGN";
    /** 不签订 */
    public static final String SIGN_ACTION_UNSIGN = "UNSIGN";

    // ========== 操作日志类型 ==========
    /** 签订 */
    public static final String OP_TYPE_SIGN = "签订";
    /** 不签订-变更 */
    public static final String OP_TYPE_UNSIGN_CHANGE = "不签订-变更";
    /** 不签订-异常归档 */
    public static final String OP_TYPE_UNSIGN_ARCHIVE_ABNORMAL = "不签订-异常归档";
}
