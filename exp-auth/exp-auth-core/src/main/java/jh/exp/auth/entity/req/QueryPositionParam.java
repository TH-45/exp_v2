package jh.exp.auth.entity.req;

import lombok.Data;

@Data
public class QueryPositionParam {
    private String postCode;
    private String postName;
    private String postType;
    private String postLevel;
    private String postCategory;



}
