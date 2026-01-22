package jh.exp.auth.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryOrgUnitReq {

    private String orgCode;
    private String orgName;

}
