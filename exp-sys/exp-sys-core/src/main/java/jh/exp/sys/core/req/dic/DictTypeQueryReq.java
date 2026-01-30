package jh.exp.sys.core.req.dic;

import lombok.Data;

@Data
public class DictTypeQueryReq {
    private String dictCode;
    private String dictName;
    private String status;
}
