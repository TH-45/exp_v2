package jh.exp.process.core.constant;

public final class ProcessConstant {

    private ProcessConstant() {
    }

    /**流程类型：合同类型**/
    public static final String PROCESS_TYPE_CONTRACT = "CONTRACT";
    /**流程类型：资金流出类合同签订流程 具体流程见下面注释-001**/
    public static final String PROCESS_CONTRACT_FUND_OUT = "CONTRACT_FUND_OUT";
    /**流程类型：资金流入类合同签核流程 具体流程见下面注释-002**/
    public static final String PROCESS_CONTRACT_FUND_IN = "CONTRACT_FUND_IN";

    /**流程类型：招投标类型**/
    public static final String PROCESS_TYPE_BID = "BID";
    /**流程类型：招投标业务流程 具体流程见下面注释-003**/
    public static final String PROCESS_BID_BUSINESS = "BID_BUSINESS";




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


    /**
     * 001
     * 资金流出类合同签订流程
     * 开始
     *  ↓
     * 创建合同
     *  ↓
     * 合同起草
     *  ↓
     * 提交
     *  ↓
     * 合同审核
     *  ├─ 不同意 → 返回合同起草
     *  └─ 同意
     *        ↓
     * 合同拟签
     *  ├─ 签订 → 正常合同归档 → 结束
     *  └─ 不签订
     *         ↓
     *       是否变更
     *       ├─ 是 → 返回合同起草
     *       └─ 否 → 异常合同归档 → 结束
     */

    /**
     * 002
     * 资金流入类合同签核流程
     * 开始
     *  ↓
     * 获取外部合同
     *  ↓
     * 整理
     *  ↓
     * 合同登记
     *  ↓
     * 提交
     *  ↓
     * 合同审核
     *  ├─ 不通过 → 返回合同登记
     *  └─ 通过
     *        ↓
     *      合同拟签
     *      ├─ 不签订 → 异常合同归档 → 结束
     *      └─ 签订
     *             ↓
     *         正常合同归档
     *             ↓
     *            结束
     */

    /**
     * 003
     * 招投标业务流程
     * 开始
     *  ↓
     * 招标录入
     *  ↓
     * 招标审批
     *  ↓
     * 是否内部招标？
     *  ├─ 是
     *  │    ↓
     *  │  发布招标信息
     *  │    ↓
     *  │  接收投标文件
     *  │    ↓
     *  │  资格预审
     *  │    ├─ 满足三家
     *  │    │      ↓
     *  │    │   评标定标
     *  │    │      ↓
     *  │    │   资金流出类合同签订流程
     *  │    │      ↓
     *  │    │     结束
     *  │    │
     *  │    └─ 不满足三家
     *  │           ↓
     *  │        招标流标归档
     *  │           ↓
     *  │        是否重新招标
     *  │           ├─ 是 → 返回 招标录入
     *  │           └─ 否 → 结束
     *  │
     *  └─ 否（外部投标）
     *       ↓
     *    制作投标文件
     *       ↓
     *    提交
     *       ↓
     *    投标审批
     *       ↓
     *    提交
     *       ↓
     *    投标
     *       ↓
     *    获取中标信息
     *       ├─ 中标
     *       │      ↓
     *       │   资金流入类合同签订流程
     *       │      ↓
     *       │      结束
     *       │
     *       └─ 不中标 / 流标
     *              ↓
     *           投标归档
     *              ↓
     *             结束
     */


}
