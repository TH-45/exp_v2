package jh.exp.auth.constant;

import java.lang.reflect.Field;

public class AuthConstant {

    /**
     * 初始账号密码 123456
     */
    public static final String INITIAL_PASSWORD = "123456";

    /**
     * 启用
     */
    public static final String ENABLED="ENABLED";
    /**
     * 禁用/停用
     */
    public static final String DISABLED="DISABLED";
    /**
     * 离职
     */
    public static final String LEAVE="LEAVE";
    /**
     * 在职
     */
    public static final String ONJOB="ONJOB";

    /**
     * 超级管理员
     */
    public static final String ADMIN="admin";

    /**
     * 判断是否在改常量中
     */
    public static String isInside(String str) {
        if (str == null) {
            throw new IllegalArgumentException("常量值不能为空");
        }

        for (Field field : AuthConstant.class.getDeclaredFields()) {
            if (field.getType() == String.class) {
                try {
                    Object value = field.get(null);
                    if (str.equals(value)) {
                        return str;
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        throw new IllegalArgumentException("该值不在规范中：" + str);
    }
}
