package jh.exp.auth.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonFlagReq {
    //标识
    private String flag;
    //人员id
    private String personId;


}
