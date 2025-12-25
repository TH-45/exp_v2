package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonInfoRes {
    private String personId;
    private String personName;
    private String mobile;
    private String personCode;
    private String jobTitle;
    private Long postId;
    private String postName;
    private Long orgId;
    private String orgName;
    private Long accountId;
    private String accountName;

}
