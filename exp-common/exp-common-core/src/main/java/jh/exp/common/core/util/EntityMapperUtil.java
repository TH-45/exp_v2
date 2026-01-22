package jh.exp.common.core.util;



import jh.exp.common.core.ext.ExtEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Map;
import java.util.function.Consumer;

public class EntityMapperUtil {

    public static <T extends ExtEntity> T copyToNewInstance(Object source, Class<T> targetClass) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            //忽略敏感信息
            String[] ignoreProperties = target.sensitiveFieldsList();
            BeanUtils.copyProperties(source, target,ignoreProperties);

            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象拷贝失败", e);
        }
    }

    public static <T extends ExtEntity> T copyToNewInstance(ExtEntity source, Class<T> targetClass,String... ignoreProperties) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            if(ignoreProperties.length == 0){
                //忽略敏感信息
                ignoreProperties=target.sensitiveFieldsList();
            }
            BeanUtils.copyProperties(source, target,ignoreProperties);

            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象拷贝失败", e);
        }
    }

    /**
     * 拷贝属性并支持通过 Map 额外赋值
     * @param source      源对象
     * @param targetClass 目标类
     * @param extraValues 额外需要赋值的字段 Map (key: 属性名, value: 属性值)
     */
    public static <T extends ExtEntity> T copyWithExtra(ExtEntity source, Class<T> targetClass, Map<String, Object> extraValues) {
        try {
            // 1. 创建新实例
            T target = targetClass.getDeclaredConstructor().newInstance();

            // 2. 基础拷贝 (source -> target)
            if (source != null) {
                BeanUtils.copyProperties(source, target);
                //忽略敏感信息
                String[] ignoreProperties = target.sensitiveFieldsList();
                BeanUtils.copyProperties(source, target,ignoreProperties);
            }

            // 3. 额外赋值 (Map -> target)
            if (extraValues != null && !extraValues.isEmpty()) {
                BeanWrapper targetWrapper = new BeanWrapperImpl(target);
                extraValues.forEach((fieldName, value) -> {
                    // 检查目标对象是否有该属性，避免抛出异常
                    if (targetWrapper.isWritableProperty(fieldName)) {
                        targetWrapper.setPropertyValue(fieldName, value);
                    }
                });
            }

            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换并赋值失败", e);
        }
    }

    /**
     * 拷贝并执行自定义后处理逻辑
     * @param processor 后处理器，例如：target -> target.setSalt(null)
     */
    public static <T extends ExtEntity> T copyToNewInstance(
            ExtEntity source,
            Class<T> targetClass,
            Consumer<T> processor) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            if (source != null) {
                BeanUtils.copyProperties(source, target);
            }

            // 如果提供了处理器，则执行（替代硬编码的 empSenInfo）
            if (processor != null) {
                processor.accept(target);
            }

            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象拷贝失败", e);
        }
    }


}
