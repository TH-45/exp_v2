package jh.exp.auth.entity.req;

import lombok.Data;

@Data
public class QueryPersonReq {
    public String personCode;
    public String name;
    public String phone;

}