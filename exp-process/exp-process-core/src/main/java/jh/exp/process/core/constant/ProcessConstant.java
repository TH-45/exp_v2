package jh.exp.process.core.constant;

public final class ProcessConstant {

    private ProcessConstant() {
    }

    /**流程类型：合同类型**/
    public static final String PROCESS_TYPE_CONTRACT = "CONTRACT";
    /**流程类型：资金流出类合同签订流程**/
    public static final String PROCESS_CONTRACT_FUND_OUT = "CONTRACT_FUND_OUT";

    /**流程类型：招投标**/
    public static final String PROCESS_TYPE_BID = "BID";




    //工单/实例的状态
    /** 流程实例状态：审批中 */
    public static final String INSTANCE_APPROVING = "APPROVING";
    /** 流程实例状态：已完成 */
    public static final String INSTANCE_COMPLETED = "COMPLETED";
    /** 流程实例状态：已拒绝 */
    public static final String INSTANCE_REJECTED = "REJECTED";
    /** 流程实例状态：已关闭 */
    public static final String INSTANCE_CLOSED = "CLOSED";


    //处理动作
    /** 操作类型：创建 */
    public static final String ACTION_CREATE = "CREATE";
    /** 操作类型：审批 */
    public static final String ACTION_APPROVE = "APPROVE";
    /** 操作类型：同意 */
    public static final String ACTION_AGREE = "AGREE";
    /** 操作类型：拒绝（直接关闭实例，不再流转） */
    public static final String ACTION_REJECT = "REJECT";
    /** 操作类型：驳回（回到上一节点，由上一个人重新审批，流程继续流转） */
    public static final String ACTION_RETURN = "RETURN";
    /** 操作类型：关闭 */
    public static final String ACTION_CLOSE = "CLOSE";


    //查询标签页
    //查询代办
    public static final String DIRECTION_TODO = "TODO";
    //查询我已处理
    public static final String DIRECTION_DONE = "DONE";
    //查询我发起的
    public static final String DIRECTION_START = "START";
    //查询我关闭
    public static final String DIRECTION_CLOSE = "CLOSE";
}
