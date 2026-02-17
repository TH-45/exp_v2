package jh.exp.auth.core.util;

import jh.exp.auth.core.entity.Menu;
import jh.exp.auth.core.entity.res.MenuTreeRes;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 菜单树构建工具类（高性能高性能版）
 */
public final class MenuTreeUtil {

    private MenuTreeUtil() {
    }

    /* ===========================
     * 对外入口
     * =========================== */

    /**
     * 构建普通菜单树（最简易版本）
     */
    public static List<MenuTreeRes> buildMenuTree(List<Menu> allMenus) {
        return buildMenuTree(allMenus, MenuTreeRes::new, null, null, null);
    }

    /**
     * 构建通用菜单树（支持任意子类 + 扩展字段）
     *
     * @param allMenus     所有原始菜单数据
     * @param nodeSupplier 节点构造器，如 MenuTreeExtRes::new  设置返回对象类型
     * @param keyExtractor 扩展字段关联键提取器，如 Menu::getMenuCode  菜单对象中哪个字段与扩展字段的值进行对比
     * @param extMap       扩展数据源 Map
     * @param extInjector  扩展字段赋值逻辑，如 (node, extValue) -> node.setExt(extValue) 设置赋值规则
     */
    public static <R extends MenuTreeRes, E, K> List<R> buildMenuTree(
            List<Menu> allMenus,
            Supplier<R> nodeSupplier,
            Function<Menu, K> keyExtractor,
            Map<K, E> extMap,
            BiConsumer<R, E> extInjector
    ) {
        if (allMenus == null || allMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 预处理：将列表按 ParentId 分组，避免递归中重复扫描列表 (O(N) 复杂度)
        Map<Long, List<Menu>> groupByParent = allMenus.stream()
                .collect(Collectors.groupingBy(m -> {
                    Long pid = m.getParentMenuId();
                    return (pid == null || pid == 0L) ? 0L : pid;
                }));

        // 2. 开始递归构建
        return buildInternal(0L, groupByParent, nodeSupplier, keyExtractor, extMap, extInjector);
    }

    /* ===========================
     * 内部递归逻辑
     * =========================== */

    private static <R extends MenuTreeRes, E, K> List<R> buildInternal(
            Long parentId,
            Map<Long, List<Menu>> groupByParent,
            Supplier<R> nodeSupplier,
            Function<Menu, K> keyExtractor,
            Map<K, E> extMap,
            BiConsumer<R, E> extInjector
    ) {
        List<Menu> currentLevelMenus = groupByParent.getOrDefault(parentId, Collections.emptyList());

        return currentLevelMenus.stream()
                .map(menu -> {
                    // 转换基础对象
                    R node = nodeSupplier.get();
                    BeanUtils.copyProperties(menu, node);

                    // 注入扩展字段（类型安全）
                    if (extInjector != null && extMap != null && keyExtractor != null) {
                        K key = keyExtractor.apply(menu);
                        E extValue = extMap.get(key);
                        if (extValue != null) {
                            extInjector.accept(node, extValue);
                        }
                    }

                    // 递归处理子节点
                    List<R> children = buildInternal(
                            menu.getMenuId(),
                            groupByParent,
                            nodeSupplier,
                            keyExtractor,
                            extMap,
                            extInjector
                    );

                    node.setChildren(new ArrayList<>(children));
                    node.setHasChildren(!children.isEmpty());
                    return node;
                })
                .sorted(menuComparator())
                .collect(Collectors.toList());
    }

    /**
     * 排序规则：sortNo 升序 (null排最后)，menuId 升序
     */
    private static Comparator<MenuTreeRes> menuComparator() {
        return Comparator.comparing(MenuTreeRes::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuTreeRes::getMenuId);
    }
}