package jh.exp.project.core.entity.res;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectMemberRes {
    private Long id;
    private Long projectId;
    private Long userId;
    private String userName;
    private String department;
    private String post;
    private Long orgId;
    private Long postId;
    private String projectRoleCode;
    private String projectRoleName;
    private Boolean isManager;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private String status;
    private String responsibilities;
}
