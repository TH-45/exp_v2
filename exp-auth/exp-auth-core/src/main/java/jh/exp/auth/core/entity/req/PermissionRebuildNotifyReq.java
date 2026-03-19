package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨服务权限重建通知请求。
 * 其他服务在主体关系变化时调用 exp-auth 的 /internal/auth/permission/rebuild/notify 接口。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionRebuildNotifyReq {

    /**
     * 事件类型：PERSON_ORG_CHANGED、POST_CHANGED、ORG_CHANGED 等
     */
    @NotBlank(message = "eventType 不能为空")
    private String eventType;

    /**
     * 主体类型：ACCOUNT/PERSON/POST/ORG
     */
    @NotBlank(message = "principalType 不能为空")
    private String principalType;

    /**
     * 主体ID列表
     */
    @NotNull(message = "principalIds 不能为空")
    private List<Long> principalIds = new ArrayList<>();

    /**
     * 来源服务标识（如 exp-corp、exp-process）
     */
    private String sourceService;

    /**
     * 操作人用户ID
     */
    private Long operatorUserId;

    /**
     * 追踪ID（用于日志关联）
     */
    private String traceId;
}
