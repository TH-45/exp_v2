package jh.exp.auth.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
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
    private String gender;
    private String email;
    private String status;
    private LocalDateTime createdTime;

    //拼接字段
    private String roleIds;
    private String roleNames;

}
