package jh.exp.auth.core.entity.node;

import java.util.List;

public class MenuNode {
    public String menuCode;
    public String menuName;
    //子节点
    public List<MenuNode> childMenuNodes;
}
