package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectMemberDeleteReq {
    @NotNull(message = "id不能为空")
    private Long id;
}
