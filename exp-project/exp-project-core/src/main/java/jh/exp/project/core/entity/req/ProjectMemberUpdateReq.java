package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectMemberUpdateReq {
    @NotNull(message = "id不能为空")
    private Long id;

    private Long userId;

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
