package jh.exp.auth.core.constant;

import java.lang.reflect.Field;

public class AuthConstant {
    /**
     * 菜单权限类型
     */
    public static final String MENU = "MENU";
    /**
     * 人员-组织-岗位状态 - 在任
     */
    public static final String STATUS_ON = "ON";

    /**
     * 人员-组织-岗位状态 - 已结束
     */
    public static final String STATUS_OFF = "OFF";

    /**
     * 人员-组织-岗位状态 - 待定
     */
    public static final String STATUS_TBD = "tbd";

    /**
     * 默认角色
     */
    public static final Long DEFAULT_ROLE = 1L;

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
     * 人员初始状态
     */
    public static final String INIT="INIT";

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
