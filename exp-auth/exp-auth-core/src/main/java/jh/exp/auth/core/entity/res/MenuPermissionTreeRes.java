package jh.exp.auth.core.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单权限树响应（用于角色「设置权限」：查权限并结构对应到树）
 * - tree：菜单树，与 /tree 结构一致
 * - selectedMenuIds：该角色已勾选的菜单 ID 列表，与树结构对应便于前端 setCheckedKeys
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuPermissionTreeRes extends MenuTreeRes {

    //权限等级(0无权、1查看、2编辑、3管理)
    private String perLevel;

}
