package jh.exp.process.core.constant;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum  ProcessDefinitionConstant {

    //资金流出类合同签订流程
    FUND_OUT_CONTRACT_SIGN("1","资金流出类合同签订流程"),
    //资金流入类合同签订流程
    FUND_IN_CONTRACT_SIGN("2","资金流入类合同签订流程"),
    //招投标业务流程
    BID_CONTRACT("3","招投标业务流程");

    
    private String code;
    private String name;



    //通过编号获取枚举实例
    public static ProcessDefinitionConstant getEnum(String code) {
        for (ProcessDefinitionConstant instance : values()) {
            if (instance.code.equals(code)) {
                return instance;
            }
        }
        return null;
    }
    //通过编号获取名称
     public static String getName(String code) {
         for (ProcessDefinitionConstant instance : values()) {
             if (instance.code.equals(code)) {
                 return instance.name;
             }
         }
         return null;

     }

}
