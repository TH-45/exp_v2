package jh.exp.common.excelProcess;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Excel 导出工具类。
 * <p>
 * 提供统一的 Excel 导出功能，支持：
 * <ul>
 *     <li>导出到 HttpServletResponse（浏览器下载）</li>
 *     <li>导出到本地文件</li>
 *     <li>分批导出（大数据量场景）</li>
 *     <li>自动设置响应头、文件名编码、样式等</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 导出到响应
 * List&lt;UserExcelModel&gt; data = userService.listAll();
 * ExcelExportUtil.exportToResponse("用户列表", UserExcelModel.class, data, response);
 *
 * // 导出到文件
 * Path filePath = Paths.get("/tmp/users.xlsx");
 * ExcelExportUtil.exportToFile(filePath, UserExcelModel.class, data);
 *
 * // 分批导出（大数据）
 * Iterator&lt;List&lt;UserExcelModel&gt;&gt; batchIterator = ...;
 * ExcelExportUtil.exportBatchToResponse("用户列表", UserExcelModel.class, batchIterator, response);
 * </pre>
 */
public class ExcelExportUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportUtil.class);

    /**
     * Excel 文件 MIME 类型
     */
    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * 默认列宽（字符数）
     */
    private static final int DEFAULT_COLUMN_WIDTH = 18;

    /**
     * 默认表头行高（磅）
     */
    private static final short DEFAULT_HEADER_ROW_HEIGHT = 22;

    /**
     * 默认数据行高（磅）
     */
    private static final short DEFAULT_DATA_ROW_HEIGHT = 18;

    /**
     * 默认表头字体大小（磅）
     */
    private static final short DEFAULT_HEADER_FONT_SIZE = 12;

    /**
     * 表格名称
     */
    private static final String SHEET_NAME = "Sheet1";

    /**
     * 导出到 HttpServletResponse（浏览器下载）。
     * <p>
     * 适用于 Controller 中直接返回 Excel 文件给前端下载的场景。
     *
     * @param fileName 文件名（不含扩展名，会自动添加 .xlsx）
     * @param headClass Excel 模型类（使用 @ExcelProperty 注解定义列）
     * @param data 要导出的数据列表
     * @param response HTTP 响应对象
     * @param <T> 数据类型
     * @throws ExcelException 导出失败时抛出
     */
    public static <T> void exportToResponse(String fileName,String sheetName,
                                            Class<T> headClass,
                                            List<T> data,
                                            HttpServletResponse response) {

        if (data == null) {
            data = Collections.emptyList();
        }

        try {
            // 设置响应头
            setResponseHeaders(fileName, response);

            // 写入 Excel
            OutputStream os = response.getOutputStream();
            EasyExcel.write(os, headClass)
                    .registerWriteHandler(createColumnWidthStrategy())
                    .registerWriteHandler(createHeaderStyleStrategy())
                    .sheet(sheetName)
                    .doWrite(data);

            os.flush();
            log.info("Excel 导出成功：文件名={}，数据行数={}", fileName, data.size());

        } catch (IOException e) {
            log.error("导出 Excel 到响应失败：{}", fileName, e);
            throw new ExcelException("导出失败：" + e.getMessage(), e);
        }
    }

    public static <T> void exportToResponse(String fileName,
                                            Class<T> headClass,
                                            List<T> data,
                                            HttpServletResponse response) {
        Objects.requireNonNull(headClass, "headClass 不能为空");
        Objects.requireNonNull(response, "response 不能为空");

        exportToResponse(fileName,SHEET_NAME, headClass, data, response);
    }

    /**
     * 导出到本地文件。
     * <p>
     * 适用于需要将 Excel 文件保存到服务器本地文件系统的场景。
     *
     * @param filePath 文件路径
     * @param headClass Excel 模型类
     * @param data 要导出的数据列表
     * @param <T> 数据类型
     * @throws ExcelException 导出失败时抛出
     */
    public static <T> void exportToFile(Path filePath,String sheetName,
                                       Class<T> headClass,
                                       List<T> data) {
        Objects.requireNonNull(filePath, "filePath 不能为空");
        Objects.requireNonNull(headClass, "headClass 不能为空");
        if (data == null) {
            data = Collections.emptyList();
        }

        try {
            // 确保父目录存在
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            // 写入 Excel
            try (OutputStream os = Files.newOutputStream(filePath)) {
                EasyExcel.write(os, headClass)
                        .registerWriteHandler(createColumnWidthStrategy())
                        .registerWriteHandler(createHeaderStyleStrategy())
                        .sheet(sheetName)
                        .doWrite(data);
            }

            log.info("Excel 导出到文件成功：文件路径={}，数据行数={}", filePath, data.size());

        } catch (IOException e) {
            log.error("导出 Excel 到文件失败：{}", filePath, e);
            throw new ExcelException("文件导出失败：" + e.getMessage(), e);
        }
    }

    public static <T> void exportToFile(Path filePath,
                                        Class<T> headClass,
                                        List<T> data) {
        Objects.requireNonNull(filePath, "filePath 不能为空");
        Objects.requireNonNull(headClass, "headClass 不能为空");

        exportToFile(filePath,SHEET_NAME, headClass, data);

    }

    /**
     * 分批导出到 HttpServletResponse（适用于大数据量场景）。
     * <p>
     * 通过迭代器分批获取数据并写入，避免一次性加载所有数据到内存。
     * <p>
     * 使用场景：
     * <ul>
     *     <li>数据量超过 10 万行</li>
     *     <li>需要从数据库分页查询并写入</li>
     *     <li>内存有限，需要流式处理</li>
     * </ul>
     *
     * @param fileName 文件名（不含扩展名）
     * @param headClass Excel 模型类
     * @param batchIterator 数据批次迭代器，每次 next() 返回一批数据（建议每批 1000-5000 行）
     * @param response HTTP 响应对象
     * @param <T> 数据类型
     * @throws ExcelException 导出失败时抛出
     */
    public static <T> void exportBatchToResponse(String fileName,String sheetName,
                                                 Class<T> headClass,
                                                 Iterator<List<T>> batchIterator,
                                                 HttpServletResponse response) {
        Objects.requireNonNull(fileName, "fileName 不能为空");
        Objects.requireNonNull(headClass, "headClass 不能为空");
        Objects.requireNonNull(batchIterator, "batchIterator 不能为空");
        Objects.requireNonNull(response, "response 不能为空");

        ExcelWriter writer = null;
        try {
            // 设置响应头
            setResponseHeaders(fileName, response);

            // 创建 ExcelWriter
            OutputStream os = response.getOutputStream();
            writer = EasyExcel.write(os, headClass)
                    .registerWriteHandler(createColumnWidthStrategy())
                    .registerWriteHandler(createHeaderStyleStrategy())
                    .build();

            WriteSheet sheet = EasyExcel.writerSheet(sheetName).build();

            // 分批写入数据
            int totalRows = 0;
            int batchCount = 0;
            while (batchIterator.hasNext()) {
                List<T> batch = batchIterator.next();
                if (batch != null && !batch.isEmpty()) {
                    writer.write(batch, sheet);
                    totalRows += batch.size();
                    batchCount++;
                    // 可选：每批写入后刷新输出流，降低内存占用
                    os.flush();
                }
            }

            log.info("分批导出 Excel 成功：文件名={}，总行数={}，批次数={}", fileName, totalRows, batchCount);

        } catch (IOException e) {
            log.error("分批导出 Excel 失败：{}", fileName, e);
            throw new ExcelException("分批导出失败：" + e.getMessage(), e);
        } finally {
            // 确保关闭 writer
            if (writer != null) {
                writer.finish();
            }
        }
    }

    public static <T> void exportBatchToResponse(String fileName,
                                                 Class<T> headClass,
                                                 Iterator<List<T>> batchIterator,
                                                 HttpServletResponse response) {
        Objects.requireNonNull(fileName, "fileName 不能为空");
        Objects.requireNonNull(headClass, "headClass 不能为空");
        Objects.requireNonNull(batchIterator, "batchIterator 不能为空");
        Objects.requireNonNull(response, "response 不能为空");

        exportBatchToResponse(fileName,SHEET_NAME, headClass, batchIterator, response);

    }

    /**
     * 设置 HTTP 响应头（Content-Type、Content-Disposition 等）。
     */
    private static void setResponseHeaders(String fileName, HttpServletResponse response) throws IOException {
        // 处理中文文件名编码
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                .replaceAll("\\+", "%20");

        response.setContentType(EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedFileName + ".xlsx");
    }

    /**
     * 创建列宽样式策略（统一列宽）。
     */
    private static SimpleColumnWidthStyleStrategy createColumnWidthStrategy() {
        return new SimpleColumnWidthStyleStrategy(DEFAULT_COLUMN_WIDTH);
    }

    /**
     * 创建表头样式策略（加粗、字体大小、行高）。
     */
    private static SimpleRowHeightStyleStrategy createHeaderStyleStrategy() {
        WriteCellStyle headStyle = new WriteCellStyle();
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints(DEFAULT_HEADER_FONT_SIZE);
        headFont.setBold(true);
        headStyle.setWriteFont(headFont);
        return new SimpleRowHeightStyleStrategy(DEFAULT_HEADER_ROW_HEIGHT, DEFAULT_DATA_ROW_HEIGHT);
    }
}
