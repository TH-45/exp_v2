package jh.exp.auth.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限字符串解析工具类
 *
 * 规则：
 * 1. 仅支持 xxx_xxx_xxx
 * 2. 第1段必须等于 head
 * 3. 第2段作为中间值（key）
 * 4. 第3段为等级：
 *    - 若 levels 是 String[]：按 index
 *    - 若 levels 是 Integer[] / int[]：按数值
 *    - 未命中：最低等级
 * 5. 批量处理中，相同中间值取等级最高
 */
public final class PermParserUtil {

    private PermParserUtil() {
    }

    /**
     * 批量解析（不传等级）
     * 默认等级：0、1、2、3 ...
     * 内部直接调用 parseBatch(List<String>, String, int[])
     */
    public static Map<String, String> parseBatch(List<String> sources, String head) {
        // 默认等级数组
        int[] defaultLevels = {0, 1, 2, 3};
        return parseBatch(sources, head, defaultLevels);
    }

    /* ========================= 对外入口 ========================= */

    public static Map<String, String> parseBatch(
            List<String> sources,
            String head,
            String[] levels
    ) {
        return parseInternal(sources, head, LevelResolver.fromStrings(levels));
    }

    public static Map<String, String> parseBatch(
            List<String> sources,
            String head,
            Integer[] levels
    ) {
        return parseInternal(sources, head, LevelResolver.fromIntegers(levels));
    }

    public static Map<String, String> parseBatch(
            List<String> sources,
            String head,
            int[] levels
    ) {
        return parseInternal(sources, head, LevelResolver.fromInts(levels));
    }

    /* ========================= 核心实现 ========================= */

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
            String[] parts = source.split("_");
            if (parts.length != 3) {
                throw new IllegalArgumentException("非法格式：" + source);
            }
            if (!head.equals(parts[0])) {
                throw new IllegalArgumentException("head 不匹配：" + source);
            }

            String middle = parts[1];
            String tail = parts[2];

            int levelValue = resolver.resolveValue(tail);
            middleLevelMap.merge(middle, levelValue, Math::max);
        }

        return resolver.toResultMap(middleLevelMap);
    }


    /* ========================= 等级解析策略 ========================= */

    private interface LevelResolver {
        int resolveValue(String tail);

        Map<String, String> toResultMap(Map<String, Integer> valueMap);

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

        static LevelResolver fromIntegers(Integer[] levels) {
            int[] ints = new int[levels.length];
            for (int i = 0; i < levels.length; i++) {
                ints[i] = levels[i];
            }
            return fromInts(ints);
        }

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
