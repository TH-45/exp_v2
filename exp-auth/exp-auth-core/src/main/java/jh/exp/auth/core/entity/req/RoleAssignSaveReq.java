package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色授权保存请求：按主体类型分别指定 ID 列表，保存时替换该角色下对应类型的所有授权。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleAssignSaveReq {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 授权给账号：accountId 列表 */
    private List<Long> accountIds = new ArrayList<>();

    /** 授权给人员：personId 列表 */
    private List<Long> personIds = new ArrayList<>();

    /** 授权给岗位：postId 列表 */
    private List<Long> postIds = new ArrayList<>();

    /** 授权给组织：orgId 列表 */
    private List<Long> orgIds = new ArrayList<>();
}
