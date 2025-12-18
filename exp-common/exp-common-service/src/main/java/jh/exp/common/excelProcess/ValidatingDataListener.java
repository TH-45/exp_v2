package jh.exp.common.excelProcess;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * EasyExcel 数据读取监听器，负责数据校验和错误收集。
 * <p>
 * 在读取 Excel 过程中，对每一行数据进行：
 * <ul>
 *     <li>JSR-303 注解校验（如 @NotNull、@NotBlank 等）</li>
 *     <li>自定义业务校验（通过 RowValidator）</li>
 *     <li>空行跳过（如果配置）</li>
 *     <li>行数限制检查</li>
 *     <li>快速失败检查</li>
 * </ul>
 *
 * @param <T> 行数据类型
 */
public class ValidatingDataListener<T> extends AnalysisEventListener<T> {

    private static final Logger log = LoggerFactory.getLogger(ValidatingDataListener.class);

    private final List<T> successList;
    private final List<ImportError> errorList;
    private final Validator validator;
    private final RowValidator<T> rowValidator;
    private final ImportOptions options;

    /**
     * 当前读取的行数（包含表头）
     */
    private int currentRowNumber = 0;

    public ValidatingDataListener(List<T> successList,
                                 List<ImportError> errorList,
                                 Validator validator,
                                 RowValidator<T> rowValidator,
                                 ImportOptions options) {
        this.successList = successList;
        this.errorList = errorList;
        this.validator = validator;
        this.rowValidator = rowValidator;
        this.options = options;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        currentRowNumber = context.readRowHolder().getRowIndex() + 1; // EasyExcel 行号从 0 开始

        // 检查最大行数限制
        if (options.getMaxRows() > 0 && currentRowNumber > options.getMaxRows()) {
            String errorMsg = String.format("超过最大允许行数 %d，已停止导入", options.getMaxRows());
            log.warn(errorMsg);
            throw new ExcelException(errorMsg);
        }

        // 检查快速失败阈值
        if (options.getFailFastThreshold() > 0 && errorList.size() >= options.getFailFastThreshold()) {
            String errorMsg = String.format("错误行数已达到快速失败阈值 %d，已停止导入", options.getFailFastThreshold());
            log.warn(errorMsg);
            throw new ExcelException(errorMsg);
        }

        // 跳过空行（简单判断：如果所有字段都为空）
        if (options.isSkipEmptyRows() && isEmptyRow(data)) {
            log.debug("跳过空行：第 {} 行", currentRowNumber);
            return;
        }

        // 执行校验
        String errorMessage = validateRow(data);

        if (errorMessage != null && !errorMessage.isBlank()) {
            // 校验失败，记录错误
            ImportError error = new ImportError(currentRowNumber, errorMessage, data);
            errorList.add(error);
            log.debug("第 {} 行校验失败：{}", currentRowNumber, errorMessage);
        } else {
            // 校验通过，加入成功列表
            successList.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel 导入完成，总行数：{}，成功：{}，失败：{}",
                currentRowNumber, successList.size(), errorList.size());
    }

    /**
     * 校验一行数据。
     * <p>
     * 校验顺序：
     * 1. JSR-303 注解校验（如果启用）
     * 2. 自定义业务校验（RowValidator）
     *
     * @param data 行数据
     * @return 错误信息，如果校验通过返回 null
     */
    private String validateRow(T data) {
        // 1. JSR-303 注解校验
        if (options.isEnableValidation() && validator != null) {
            Set<ConstraintViolation<T>> violations = validator.validate(data);
            if (!violations.isEmpty()) {
                List<String> messages = new ArrayList<>();
                for (ConstraintViolation<T> violation : violations) {
                    messages.add(violation.getPropertyPath() + ": " + violation.getMessage());
                }
                return String.join("; ", messages);
            }
        }

        // 2. 自定义业务校验
        if (rowValidator != null) {
            try {
                return rowValidator.validate(data);
            } catch (Exception e) {
                log.error("执行自定义校验时发生异常，第 {} 行", currentRowNumber, e);
                return "自定义校验异常：" + e.getMessage();
            }
        }

        return null;
    }

    /**
     * 判断是否为空行。
     * <p>
     * 简单实现：检查对象的所有字段是否都为空。
     * 注意：此实现较为简单，对于复杂对象可能需要更精细的判断逻辑。
     */
    private boolean isEmptyRow(T data) {
        if (data == null) {
            return true;
        }
        // 简单判断：如果对象的所有字段都是 null 或空字符串，认为是空行
        // 实际项目中可以根据具体需求实现更精确的空行判断逻辑
        return data.toString().trim().isEmpty();
    }
}




