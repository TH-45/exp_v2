package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectMemberCreateReq {
    @NotNull(message = "projectId不能为空")
    private Long projectId;

    @NotNull(message = "userId不能为空")
    private Long userId;

    private Long orgId;

    private Long postId;

    private String projectRoleCode;

    private String projectRoleName;

    private Boolean isManager;

    @NotNull(message = "joinDate不能为空")
    private LocalDate joinDate;

    private LocalDate leaveDate;

    private String status;

    private String responsibilities;
}
