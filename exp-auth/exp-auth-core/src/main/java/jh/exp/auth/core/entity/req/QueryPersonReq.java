package jh.exp.auth.core.entity.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryPersonReq {
    public String personCode;
    public String personName;
    public String mobile;

}