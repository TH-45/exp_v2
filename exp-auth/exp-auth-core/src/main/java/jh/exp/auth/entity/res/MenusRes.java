package jh.exp.auth.entity.res;

import jh.exp.auth.entity.node.MenuNode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class MenusRes {
    public List<Long> roleId;
    public List<String> roleCode;
    public List<String> roleName;
    public List<MenuNode> menus;

}
