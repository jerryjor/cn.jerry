package cn.jerry.fabric.api.fabric.exception;

public class NotExistsException extends Exception {
    private final String message;

    public NotExistsException(String name) {
        this.message = String.format("%s not exists.", name);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
