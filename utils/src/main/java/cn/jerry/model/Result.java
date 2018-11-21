package cn.jerry.model;

import java.io.Serializable;

import cn.jerry.json.JsonUtil;

public class Result<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private ResultCode code;
	private String message;
	private T data;

	public Result() {
		super();
	}

	public Result(ResultCode code, String message, T data) {
		super();
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public ResultCode getCode() {
		return code;
	}

	public void setCode(ResultCode code) {
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
		try {
			return JsonUtil.toJsonNonNull(this);
		} catch (Exception e) {
			return "Result ["
			        + (code != null ? "code=" + code + ", " : "")
			        + (message != null ? "message=" + message + ", " : "")
			        + (data != null ? "data=" + data : "")
			        + "]";
		}
	}

}
