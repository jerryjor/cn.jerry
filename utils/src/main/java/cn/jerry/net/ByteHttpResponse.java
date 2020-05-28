package cn.jerry.net;

public class ByteHttpResponse {
    private int statusCode;
    private byte[] entity;

    public ByteHttpResponse() {
        super();
    }

    public ByteHttpResponse(int statusCode, byte[] entity) {
        this.statusCode = statusCode;
        this.entity = entity;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getEntity() {
        return entity;
    }

    public void setEntity(byte[] entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "{\"statusCode\":" + statusCode + ", \"entity.length\":" + (entity == null ? 0 : entity.length) + "}";
    }
}
