package jh.exp.common.excelProcess;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Excel 导入工具类。
 * <p>
 * 提供统一的 Excel 导入功能，支持：
 * <ul>
 *     <li>从 MultipartFile 导入（Spring MVC 文件上传）</li>
 *     <li>从 InputStream 导入（通用输入流）</li>
 *     <li>从本地文件导入</li>
 *     <li>数据校验（JSR-303 注解 + 自定义业务校验）</li>
 *     <li>错误收集和报告</li>
 *     <li>空行跳过、行数限制、快速失败等配置</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 从 MultipartFile 导入
 * ImportOptions options = new ImportOptions();
 * options.setMaxRows(10000);
 * options.setFailFastThreshold(100);
 *
 * RowValidator&lt;UserExcelModel&gt; validator = (row) -&gt; {
 *     if (userService.existsByUsername(row.getUsername())) {
 *         return "用户名已存在：" + row.getUsername();
 *     }
 *     return null;
 * };
 *
 * ImportResult&lt;UserExcelModel&gt; result = ExcelImportUtil.importFromMultipartFile(
 *     file, UserExcelModel.class, options, validator);
 *
 * if (result.hasError()) {
 *     // 处理错误
 *     result.getErrorList().forEach(error -&gt; {
 *         log.error("第 {} 行：{}", error.getRowNumber(), error.getErrorMessage());
 *     });
 * }
 *
 * // 处理成功的数据
 * userService.batchSave(result.getSuccessList());
 * </pre>
 */
public class ExcelImportUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportUtil.class);

    /**
     * Excel 文件 MIME 类型
     */
    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * JSR-303 校验器（单例，避免重复创建）
     */
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 从 MultipartFile 导入 Excel。
     * <p>
     * 适用于 Spring MVC Controller 中接收文件上传的场景。
     *
     * @param file 上传的文件
     * @param headClass Excel 模型类（使用 @ExcelProperty 注解定义列）
     * @param options 导入配置选项（可为 null，使用默认配置）
     * @param rowValidator 自定义行校验器（可为 null，不进行自定义校验）
     * @param <T> 数据类型
     * @return 导入结果，包含成功数据列表和错误信息列表
     * @throws ExcelException 文件格式错误、读取失败等异常
     */
    public static <T> ImportResult<T> importFromMultipartFile(MultipartFile file,
                                                               Class<T> headClass,
                                                               ImportOptions options,
                                                               RowValidator<T> rowValidator) {
        Objects.requireNonNull(file, "file 不能为空");
        Objects.requireNonNull(headClass, "headClass 不能为空");

        // 文件类型校验
        validateFileType(file);

        try (InputStream is = file.getInputStream()) {
            return importFromInputStream(is, headClass, options, rowValidator);
        } catch (IOException e) {
            log.error("读取上传文件失败：{}", file.getOriginalFilename(), e);
            throw new ExcelException("读取上传文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从 InputStream 导入 Excel。
     * <p>
     * 适用于从任意输入流读取 Excel 的场景（如从文件系统、网络等）。
     *
     * @param inputStream 输入流（调用方负责关闭）
     * @param headClass Excel 模型类
     * @param options 导入配置选项（可为 null，使用默认配置）
     * @param rowValidator 自定义行校验器（可为 null）
     * @param <T> 数据类型
     * @return 导入结果
     * @throws ExcelException 解析失败等异常
     */
    public static <T> ImportResult<T> importFromInputStream(InputStream inputStream,
                                                            Class<T> headClass,
                                                            ImportOptions options,
                                                            RowValidator<T> rowValidator) {
        Objects.requireNonNull(inputStream, "inputStream 不能为空");
        Objects.requireNonNull(headClass, "headClass 不能为空");

        // 使用默认配置（如果未提供）
        if (options == null) {
            options = new ImportOptions();
        }

        // 创建结果容器
        ImportResult<T> result = new ImportResult<>();
        java.util.List<T> successList = result.getSuccessList();
        java.util.List<ImportError> errorList = result.getErrorList();

        // 创建数据监听器
        ValidatingDataListener<T> listener = new ValidatingDataListener<>(
                successList, errorList, VALIDATOR, rowValidator, options);

        try {
            // 读取 Excel
            EasyExcel.read(inputStream, headClass, listener)
                    .headRowNumber(options.getHeadRowNumber())
                    .sheet(options.getSheetNo())
                    .doRead();

            // 统计结果
            result.setTotalRows(successList.size() + errorList.size());
            result.setSuccessRows(successList.size());
            result.setErrorRows(errorList.size());

            log.info("Excel 导入完成：总行数={}，成功={}，失败={}",
                    result.getTotalRows(), result.getSuccessRows(), result.getErrorRows());

        } catch (ExcelException e) {
            // 业务中断（如超过最大行数、快速失败等）
            // 保留已收集的数据与错误
            result.setTotalRows(successList.size() + errorList.size());
            result.setSuccessRows(successList.size());
            result.setErrorRows(errorList.size());
            log.warn("Excel 导入被中断：{}", e.getMessage());
        } catch (Exception e) {
            log.error("解析 Excel 失败", e);
            throw new ExcelException("解析失败：" + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 从本地文件导入 Excel。
     * <p>
     * 适用于从服务器本地文件系统读取 Excel 的场景。
     *
     * @param filePath 文件路径
     * @param headClass Excel 模型类
     * @param options 导入配置选项（可为 null）
     * @param rowValidator 自定义行校验器（可为 null）
     * @param <T> 数据类型
     * @return 导入结果
     * @throws ExcelException 文件不存在、读取失败等异常
     */
    public static <T> ImportResult<T> importFromFile(Path filePath,
                                                    Class<T> headClass,
                                                    ImportOptions options,
                                                    RowValidator<T> rowValidator) {
        Objects.requireNonNull(filePath, "filePath 不能为空");
        Objects.requireNonNull(headClass, "headClass 不能为空");

        if (!Files.exists(filePath)) {
            throw new ExcelException("文件不存在：" + filePath);
        }

        try (InputStream is = Files.newInputStream(filePath)) {
            return importFromInputStream(is, headClass, options, rowValidator);
        } catch (IOException e) {
            log.error("读取文件失败：{}", filePath, e);
            throw new ExcelException("读取文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 生成导入错误报告 Excel 文件。
     * <p>
     * 将导入失败的错误信息导出为 Excel，便于用户查看和修正。
     *
     * @param errors 错误列表
     * @param fileName 导出文件名
     * @param response HTTP 响应对象
     */
    public static void exportErrorReport(java.util.List<ImportError> errors,
                                        String fileName,
                                        HttpServletResponse response) {
        Objects.requireNonNull(errors, "errors 不能为空");
        Objects.requireNonNull(fileName, "fileName 不能为空");
        Objects.requireNonNull(response, "response 不能为空");

        // 将 ImportError 转换为简单的错误报告模型
        java.util.List<ErrorReportModel> reportData = errors.stream()
                .map(error -> {
                    ErrorReportModel model = new ErrorReportModel();
                    model.setRowNumber(error.getRowNumber());
                    model.setErrorMessage(error.getErrorMessage());
                    return model;
                })
                .toList();

        // 导出错误报告
        ExcelExportUtil.exportToResponse(fileName, ErrorReportModel.class, reportData, response);
    }

    /**
     * 校验文件类型。
     * <p>
     * 目前仅支持 .xlsx 格式（Excel 2007+）。
     * 可扩展支持 .xls 格式（需要额外依赖）。
     *
     * @param file 上传的文件
     * @throws ExcelException 文件类型不支持
     */
    private static void validateFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ExcelException("文件名不能为空");
        }

        String lowerName = originalFilename.toLowerCase(Locale.ROOT);
        if (!lowerName.endsWith(".xlsx")) {
            throw new ExcelException("仅支持 .xlsx 格式的 Excel 文件");
        }

        // 可选：MIME 类型校验
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals(EXCEL_CONTENT_TYPE)) {
            log.warn("文件 MIME 类型不匹配：期望 {}，实际 {}", EXCEL_CONTENT_TYPE, contentType);
            // 不强制失败，因为某些浏览器可能发送错误的 MIME 类型
        }

        // 可选：文件大小限制（由 Spring Boot 配置 multipart.max-file-size 控制）
        // 这里可以额外检查，例如最大 50MB
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new ExcelException("文件大小超过限制（最大 50MB）");
        }
    }

    /**
     * 错误报告模型（用于导出错误报告 Excel）。
     */
    public static class ErrorReportModel {
        @com.alibaba.excel.annotation.ExcelProperty("行号")
        private Integer rowNumber;

        @com.alibaba.excel.annotation.ExcelProperty("错误信息")
        private String errorMessage;

        public Integer getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(Integer rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
