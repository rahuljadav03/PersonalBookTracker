package com.example.booktracker.data;

public class DbResult<T> {
    public enum ErrorCode {
        SUCCESS,
        DUPLICATE,
        CONSTRAINT_FAILED,
        ERROR
    }

    private final T data;
    private final ErrorCode errorCode;
    private final String errorMessage;

    private DbResult(T data, ErrorCode errorCode, String errorMessage) {
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static <T> DbResult<T> success(T data) {
        return new DbResult<>(data, ErrorCode.SUCCESS, null);
    }

    public static <T> DbResult<T> error(ErrorCode code, String message) {
        return new DbResult<>(null, code, message);
    }

    public T getData() {
        return data;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return errorCode == ErrorCode.SUCCESS;
    }
}
