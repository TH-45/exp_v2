package jh.exp.common.core.exception;

import java.io.Serial;

public abstract class GlobalException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private String message;
    private String code;
    private int httpStatus;

    public abstract String getMessage();
    public abstract String getCode();
    public abstract int getHttpStatus();

}
