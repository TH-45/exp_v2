package jh.exp.auth.entity.Req;

import lombok.Data;

@Data
public class QueryPositionReq {
    private String postCode;
    private String postName;
    private String postType;
    private String postLevel;
    private String postCategory;



}
