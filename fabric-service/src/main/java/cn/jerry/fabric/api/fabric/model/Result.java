package cn.jerry.fabric.api.fabric.model;

import java.io.Serializable;

public class Result<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 3576356845725428115L;

    public static final String CODE_SUCCEED = "0000";
    public static final String CODE_PARAM_EMPTY = "1000";
    public static final String CODE_FAILED = "2000";
    public static final String CODE_IN_PROCESS = "2001";
    public static final String CODE_DUPLICATE = "2002";
    public static final String CODE_SYS_ERR = "9999";

    private String code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(String code, String message) {
        this.code = code;
        this.message = message;
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

    @Override
    public String toString() {
        return String.format("{\"code\":\"%s\", \"message\":\"%s\"}", code, message);
    }
}
