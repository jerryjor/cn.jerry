package cn.jerry.fabric.api.fabric.exception;

public class AmountNegativeException extends RuntimeException {
    private final String message;

    public AmountNegativeException(Number amount) {
        this.message = String.format("Amount[%s] can not be negative.", amount);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
