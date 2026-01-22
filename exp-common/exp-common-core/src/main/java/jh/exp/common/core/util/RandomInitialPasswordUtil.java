package jh.exp.common.core.util;

import java.security.SecureRandom;

public class RandomInitialPasswordUtil {
    // 固定前缀：exp + 000
    private static final String FIXED_PREFIX = "exp000";
    // 6位随机字符的长度
    private static final int RANDOM_LENGTH = 6;
    // 字符池：数字 + 小写字母 + 大写字母
    private static final String CHARACTER_POOL = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // 线程安全的安全随机数生成器
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成格式为 exp000 + 6位随机数字字母组合的字符串
     * @return 符合格式的随机字符串，例如 exp000a89Z78
     */
    public static String getExpRandomId() {
        // 构建字符串拼接器，效率更高
        StringBuilder sb = new StringBuilder(FIXED_PREFIX);

        // 循环生成6位随机字符
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            // 随机获取字符池中的索引
            int randomIndex = SECURE_RANDOM.nextInt(CHARACTER_POOL.length());
            // 拼接随机字符
            sb.append(CHARACTER_POOL.charAt(randomIndex));
        }

        return sb.toString();
    }

    // 测试方法
    public static void main(String[] args) {
        // 生成10个示例，验证效果
        for (int i = 0; i < 10; i++) {
            String result = getExpRandomId();
            System.out.println("生成的字符串：" + result);
        }
    }
}
