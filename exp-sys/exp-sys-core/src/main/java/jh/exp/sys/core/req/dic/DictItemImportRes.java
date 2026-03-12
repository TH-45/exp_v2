package jh.exp.sys.core.req.dic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典项导入结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictItemImportRes {
    /** 成功数量 */
    private int successCount;
    /** 失败数量 */
    private int failCount;
    /** 错误明细（行号或索引 + 错误信息） */
    private List<String> errors = new ArrayList<>();
}
