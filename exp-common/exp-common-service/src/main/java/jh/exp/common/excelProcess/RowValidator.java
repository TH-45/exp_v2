package jh.exp.common.excelProcess;

/**
 * Excel 导入行数据校验器接口。
 * <p>
 * 用于在导入过程中对每一行数据进行自定义业务校验。
 * 例如：检查唯一性、关联数据是否存在、业务规则校验等。
 *
 * @param <T> 行数据类型
 */
@FunctionalInterface
public interface RowValidator<T> {

    /**
     * 校验一行数据。
     *
     * @param rowData 当前行的数据对象
     * @return 如果校验通过返回 null，否则返回错误信息字符串
     */
    String validate(T rowData);
}




