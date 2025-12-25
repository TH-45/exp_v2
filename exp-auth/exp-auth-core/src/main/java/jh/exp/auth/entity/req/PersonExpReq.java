package jh.exp.auth.entity.req;

import jh.exp.auth.entity.ExpPerson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;


@Data
public class PersonExpReq  {

    private Long personId;
    private String personCode;
    private String personName;
    private String gender;
    private String mobile;
    private String email;
    private String idCardNo;
    private String jobTitle;
    private Long orgId;
    private Long postId;
    private Long accountId;
    private String status;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    // 是否外部人员/合作方（0/1）
    private Integer isExternal;
    private String remark;


    private String orgName;
    private String postName;
    private String accountName;



}
