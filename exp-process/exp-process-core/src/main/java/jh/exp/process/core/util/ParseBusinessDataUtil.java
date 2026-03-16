package jh.exp.process.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Objects;

public class ParseBusinessDataUtil {

    /**
     *  解析业务数据
     * @param businessData
     * @return
     */
    public static Object parse(Object businessData) {

        if (businessData == null) {
            throw new RuntimeException("businessData 为空");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> map = mapper.convertValue(businessData, Map.class);

            Object code = map.get("code");

            if (!Objects.equals(code, 0) && !Objects.equals(code, "0")) {
                throw new RuntimeException("业务处理失败，code=" + code);
            }

            return map.get("data");

        } catch (Exception e) {
            throw new RuntimeException("解析 businessData 失败", e);
        }
    }
}
