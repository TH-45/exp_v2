package jh.exp.auth.core.entity.req;

import lombok.Data;

@Data
public class PersonFlagReq {
    //标识
    private String flag;
    //人员id
    private String personId;
}
