package jh.exp.common.core.exception;

/**
 * 鉴权异常，用于权限校验失败时抛出。
 * 支持携带错误码，便于前端区分 AUTH_FORBIDDEN / AUTH_PERMISSION_CHANGED 等。
 */
public class AuthException extends RuntimeException {
    private final String code;

    public AuthException(String message) {
        super(message);
        this.code = "AUTH_FORBIDDEN";
    }

    public AuthException(String code, String message) {
        super(message);
        this.code = code != null ? code : "AUTH_FORBIDDEN";
    }

    public String getCode() {
        return code;
    }
}
