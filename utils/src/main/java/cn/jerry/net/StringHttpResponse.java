package cn.jerry.net;

public class StringHttpResponse {
    private int statusCode;
    private String entity;

    public StringHttpResponse() {
    }

    public StringHttpResponse(int statusCode, String entity) {
        this.statusCode = statusCode;
        this.entity = entity;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "{\"statusCode\":" + statusCode + ", \"entity.length\":" + (entity == null ? 0 : entity.length()) + "}";
    }
}
