package jh.exp.auth.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限解析工具类
 * 提供批量解析权限字符串的功能，支持多种等级表示方式
 * 支持多段式权限格式：head_middle_part1_middle_part2_..._level
 * 例如：MENU_ACCOUNT_MGR_1
 */
public final class PermParserUtil {

    private PermParserUtil() {
    }

    /* ========================= 对外入口 ========================= */

    /**
     * 批量解析权限字符串（使用默认等级）
     * 默认等级：0、1、2、3 ...
     *
     * @param sources 权限字符串列表，格式为 "head_middle_..._level"
     * @param head    头部标识，用于校验权限字符串格式
     * @return 解析后的权限映射，key为middle部分（含下划线），value为对应的最高等级
     */
    public static Map<String, String> parseBatch(List<String> sources, String head) {
        // 默认等级数组
        int[] defaultLevels = {0, 1, 2, 3};
        return parseBatch(sources, head, defaultLevels);
    }

    /**
     * 批量构建权限字符串
     *
     * @param middles 中间部分列表（可包含下划线）
     * @param head    头部标识
     * @param level   等级字符串
     * @return 构建后的权限字符串列表，格式为 "head_middle_level"
     */
    public static List<String> buildBatch(List<String> middles, String head, String level) {
        if (middles == null || middles.isEmpty()) {
            return List.of();
        }

        return middles.stream()
                .map(middle -> head + "_" + middle + "_" + level)
                .toList();
    }

    /**
     * 构建权限字符串（整数等级）
     *
     * @param head   头部标识
     * @param middle 中间部分（可包含下划线）
     * @param level  等级（整数）
     * @return 构建后的权限字符串，格式为 "head_middle_level"
     */
    public static String build(String head, String middle, int level) {
        return build(head, middle, String.valueOf(level));
    }

    /**
     * 构建权限字符串（字符串等级）
     *
     * @param head   头部标识
     * @param middle 中间部分（可包含下划线）
     * @param level  等级（字符串）
     * @return 构建后的权限字符串，格式为 "head_middle_level"
     * @throws IllegalArgumentException 当 head、middle 或 level 为 null 时抛出
     */
    public static String build(String head, String middle, String level) {
        if (head == null || middle == null || level == null) {
            throw new IllegalArgumentException("head/middle/level 不能为空");
        }
        return head + "_" + middle + "_" + level;
    }

    /**
     * 批量解析权限字符串（字符串等级数组）
     *
     * @param sources 权限字符串列表，格式为 "head_middle_..._level"
     * @param head    头部标识，用于校验权限字符串格式
     * @param levels  等级字符串数组，如 ["low", "medium", "high"]
     * @return 解析后的权限映射，key为middle部分，value为对应的最高等级字符串
     */
    public static Map<String, String> parseBatch(
            List<String> sources,
            String head,
            String[] levels
    ) {
        return parseInternal(sources, head, LevelResolver.fromStrings(levels));
    }

    /**
     * 批量解析权限字符串（Integer等级数组）
     *
     * @param sources 权限字符串列表，格式为 "head_middle_..._level"
     * @param head    头部标识，用于校验权限字符串格式
     * @param levels  等级Integer数组，如 [1, 2, 3]
     * @return 解析后的权限映射，key为middle部分，value为对应的最高等级字符串
     */
    public static Map<String, String> parseBatch(
            List<String> sources,
            String head,
            Integer[] levels
    ) {
        return parseInternal(sources, head, LevelResolver.fromIntegers(levels));
    }

    /**
     * 批量解析权限字符串（int等级数组）
     *
     * @param sources 权限字符串列表，格式为 "head_middle_..._level"
     * @param head    头部标识，用于校验权限字符串格式
     * @param levels  等级int数组，如 [1, 2, 3]
     * @return 解析后的权限映射，key为middle部分，value为对应的最高等级字符串
     */
    public static Map<String, String> parseBatch(
            List<String> sources,
            String head,
            int[] levels
    ) {
        return parseInternal(sources, head, LevelResolver.fromInts(levels));
    }

    /* ========================= 核心实现 ========================= */

    /**
     * 内部核心解析方法
     * 支持多段式格式：head_middle_part1_middle_part2_..._level
     * 例如：MENU_ACCOUNT_MGR_1 -> head=MENU, middle=ACCOUNT_MGR, level=1
     *
     * @param sources  权限字符串列表
     * @param head     头部标识
     * @param resolver 等级解析策略
     * @return 解析后的权限映射
     */
    private static Map<String, String> parseInternal(
            List<String> sources,
            String head,
            LevelResolver resolver
    ) {
        if (sources == null || sources.isEmpty()) {
            return Map.of();
        }

        Map<String, Integer> middleLevelMap = new HashMap<>();

        for (String source : sources) {
            // 至少需要3段：head + 至少1段middle + level
            int firstUnderline = source.indexOf('_');
            int lastUnderline = source.lastIndexOf('_');

            // 校验格式：必须至少有两个下划线
            if (firstUnderline == -1 || lastUnderline == -1 || firstUnderline == lastUnderline) {
                throw new IllegalArgumentException("非法格式（至少需要 head_middle_level 三段）：" + source);
            }

            String sourceHead = source.substring(0, firstUnderline);
            if (!head.equals(sourceHead)) {
                throw new IllegalArgumentException("head 不匹配，期望[" + head + "]，实际[" + sourceHead + "]：" + source);
            }

            // middle 是第一个下划线和最后一个下划线之间的部分（可包含下划线）
            String middle = source.substring(firstUnderline + 1, lastUnderline);
            // level 是最后一个下划线之后的部分
            String level = source.substring(lastUnderline + 1);

            if (middle.isEmpty()) {
                throw new IllegalArgumentException("middle 不能为空：" + source);
            }
            if (level.isEmpty()) {
                throw new IllegalArgumentException("level 不能为空：" + source);
            }

            int levelValue = resolver.resolveValue(level);
            middleLevelMap.merge(middle, levelValue, Math::max);
        }

        return resolver.toResultMap(middleLevelMap);
    }


    /* ========================= 等级解析策略 ========================= */

    /**
     * 等级解析策略接口
     * 定义了如何解析尾部等级值以及如何转换为结果映射
     */
    private interface LevelResolver {
        /**
         * 解析尾部字符串为等级值
         *
         * @param tail 尾部字符串
         * @return 对应的等级值
         */
        int resolveValue(String tail);

        /**
         * 将中间部分与等级值的映射转换为最终结果映射
         *
         * @param valueMap 中间部分到等级值的映射
         * @return 最终的结果映射
         */
        Map<String, String> toResultMap(Map<String, Integer> valueMap);

        /**
         * 创建基于字符串数组的解析策略
         *
         * @param levels 等级字符串数组
         * @return 字符串解析策略实例
         */
        static LevelResolver fromStrings(String[] levels) {
            return new LevelResolver() {
                @Override
                public int resolveValue(String tail) {
                    for (int i = 0; i < levels.length; i++) {
                        if (levels[i].equalsIgnoreCase(tail)) {
                            return i;
                        }
                    }
                    return 0;
                }

                @Override
                public Map<String, String> toResultMap(Map<String, Integer> valueMap) {
                    Map<String, String> result = new HashMap<>();
                    valueMap.forEach((k, v) ->
                            result.put(k, v < levels.length ? levels[v] : String.valueOf(v))
                    );
                    return result;
                }
            };
        }

        /**
         * 创建基于Integer数组的解析策略
         *
         * @param levels 等级Integer数组
         * @return Integer解析策略实例
         */
        static LevelResolver fromIntegers(Integer[] levels) {
            int[] ints = new int[levels.length];
            for (int i = 0; i < levels.length; i++) {
                ints[i] = levels[i];
            }
            return fromInts(ints);
        }

        /**
         * 创建基于int数组的解析策略
         *
         * @param levels 等级int数组
         * @return int解析策略实例
         */
        static LevelResolver fromInts(int[] levels) {
            return new LevelResolver() {
                @Override
                public int resolveValue(String tail) {
                    try {
                        return Integer.parseInt(tail);
                    } catch (NumberFormatException e) {
                        return levels.length > 0 ? levels[0] : 0;
                    }
                }

                @Override
                public Map<String, String> toResultMap(Map<String, Integer> valueMap) {
                    Map<String, String> result = new HashMap<>();
                    valueMap.forEach((k, v) -> result.put(k, String.valueOf(v)));
                    return result;
                }
            };
        }
    }

}