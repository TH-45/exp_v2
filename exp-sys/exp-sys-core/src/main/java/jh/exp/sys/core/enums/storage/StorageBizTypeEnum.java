package jh.exp.sys.core.enums.storage;

import lombok.Getter;

@Getter
public enum StorageBizTypeEnum {
    TENDER("TENDER", "招标"),
    BID("BID", "投标"),
    COMMON("COMMON", "通用");

    private final String code;
    private final String desc;

    StorageBizTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
