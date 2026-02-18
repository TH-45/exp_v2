package jh.exp.auth.core.entity.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@Data
@AllArgsConstructor
public class MenuNode {
    public String menuCode;
    public String menuName;
    //子节点
    public List<MenuNode> childMenuNodes;
}
