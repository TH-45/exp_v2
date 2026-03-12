package jh.exp.sys.service.dic.strategy;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.StringUtils;

/**
 * 字典导出时合并字典类型列（0-3 列）
 * 相同 dictCode 的连续行合并类型相关列
 */
public class DictTypeMergeStrategy extends AbstractMergeStrategy {

    private static final int[] MERGE_COLUMNS = {0, 1, 2, 3};
    private int lastDataRowIndex = -1;

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        int rowIndex = cell.getRowIndex();
        int colIndex = cell.getColumnIndex();
        if (rowIndex < 1 || colIndex > 3) return; // 跳过表头及非类型列
        lastDataRowIndex = Math.max(lastDataRowIndex, rowIndex);
        if (colIndex != 0) return; // 只在处理第 0 列时计算合并
        String curVal = getCellStringValue(cell);
        if (rowIndex == 1) return; // 第一行数据，无需合并
        Row prevRow = sheet.getRow(rowIndex - 1);
        if (prevRow == null) return;
        Cell prevCell = prevRow.getCell(0);
        String prevVal = prevCell != null ? getCellStringValue(prevCell) : "";
        if (StringUtils.hasText(curVal) && !curVal.equals(prevVal)) {
            // 遇到新类型，合并上一组
            int startRow = findGroupStartRow(sheet, rowIndex - 1);
            if (startRow < rowIndex - 1) {
                for (int c : MERGE_COLUMNS) {
                    sheet.addMergedRegion(new CellRangeAddress(startRow, rowIndex - 1, c, c));
                }
            }
        }
    }

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        super.afterCellDispose(context);
        if (lastDataRowIndex < 1) return;
        Sheet sheet = context.getWriteSheetHolder().getSheet();
        int rowIndex = context.getCell().getRowIndex();
        int colIndex = context.getCell().getColumnIndex();
        if (rowIndex == lastDataRowIndex && colIndex == 0) {
            int startRow = findGroupStartRow(sheet, lastDataRowIndex);
            if (startRow < lastDataRowIndex) {
                for (int c : MERGE_COLUMNS) {
                    if (!isInMergedRegion(sheet, lastDataRowIndex, c)) {
                        sheet.addMergedRegion(new CellRangeAddress(startRow, lastDataRowIndex, c, c));
                    }
                }
            }
        }
    }

    private int findGroupStartRow(Sheet sheet, int fromRow) {
        if (fromRow <= 1) return 1;
        String val = getCellStringValue(sheet.getRow(fromRow).getCell(0));
        for (int r = fromRow - 1; r >= 1; r--) {
            Row row = sheet.getRow(r);
            if (row == null) return r + 1;
            Cell c = row.getCell(0);
            if (c == null || !val.equals(getCellStringValue(c))) return r + 1;
        }
        return 1;
    }

    private boolean isInMergedRegion(Sheet sheet, int row, int col) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(row, col)) return true;
        }
        return false;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
