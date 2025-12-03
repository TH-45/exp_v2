package jh.exp.common.api;

/**
 * 统一响应结构，与前端 axios 封装及 docs 中的约定保持一致。
 *
 * <pre>
 * interface ApiResponse&lt;T&gt; {
 *   boolean success;
 *   String  code;      // "0" 表示成功
 *   String  message;
 *   T       data;
 * }
 * </pre>
 */
public class ApiResponse<T> {

    /**
     * 业务是否成功
     */
    private boolean success;

    /**
     * 业务编码；成功为 "0"，失败时为业务错误码
     */
    private String code;

    /**
     * 提示或错误信息
     */
    private String message;

    /**
     * 真实业务数据
     */
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "0", "OK", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "0", message, data);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}












