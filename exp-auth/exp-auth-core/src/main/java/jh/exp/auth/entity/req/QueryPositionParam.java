package jh.exp.auth.entity.req;

import lombok.Data;

@Data
public class QueryPositionParam {
    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 岗位状态
     */
    private String status;

    /**
     * 岗位类型
     */
    private String postType;

//    /**
//     * 岗位级别
//     */
//    private String postLevel;
//
//    /**
//     * 岗位类别
//     */
//    private String postCategory;

}
