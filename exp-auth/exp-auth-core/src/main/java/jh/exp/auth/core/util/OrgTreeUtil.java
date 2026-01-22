package jh.exp.auth.core.util;


import jh.exp.auth.core.entity.OrgUnit;

import jh.exp.auth.core.entity.res.OrgUnitTreeRes;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class OrgTreeUtil {

    /**
     * 构建组织树
     */
    public static List<OrgUnitTreeRes> buildOrgTree(List<OrgUnit> allOrgs, Long parentId) {
        return allOrgs.stream()
                .filter(org -> {
                    Long orgParentId = org.getParentOrgId();
                    // 处理根节点：parentId为null或0的情况
                    if (parentId == null) {
                        return orgParentId == null || orgParentId == 0;
                    } else if (parentId == 0) {
                        return orgParentId == null || orgParentId.equals(0L);
                    } else {
                        return parentId.equals(orgParentId);
                    }
                })
                .map(org -> {
                    OrgUnitTreeRes node = new OrgUnitTreeRes();
                    BeanUtils.copyProperties(org, node);
                    List<OrgUnitTreeRes> children = buildOrgTree(allOrgs, org.getOrgId());
                    node.setChildren(children);
                    node.setHasChildren(!children.isEmpty());
                    return node;
                })
                .sorted((a, b) -> {
                    // 按排序号排序，如果排序号相同则按组织ID排序
                    if (a.getSortNo() != null && b.getSortNo() != null) {
                        int sortCompare = a.getSortNo().compareTo(b.getSortNo());
                        if (sortCompare != 0) {
                            return sortCompare;
                        }
                    }
                    // 如果排序号相同或为null，按组织ID排序
                    return a.getOrgId().compareTo(b.getOrgId());
                })
                .collect(Collectors.toList());
    }


}
