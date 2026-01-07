package jh.exp.common.ext;

import java.util.ArrayList;
import java.util.List;

public interface ExtEntity {

    /**
     * 置空敏感信息
     */
    default String[] sensitiveFieldsList(){
        return new String [0];
    };
}
