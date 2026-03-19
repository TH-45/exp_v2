package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色授权查询响应：按主体类型分组返回。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleAssignRes {

    private Long roleId;

    /** 按主体类型分组的授权列表 */
    private List<PrincipalGroup> groups = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrincipalGroup {
        /** 主体类型：ACCOUNT/PERSON/POST/ORG */
        private String principalType;
        /** 主体类型中文名 */
        private String principalTypeName;
        /** 授权项列表 */
        private List<PrincipalItem> items = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrincipalItem {
        private Long id;
        private Long principalId;
        private String principalCode;
        private String principalName;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
