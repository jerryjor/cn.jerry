package cn.jerry.blockchain.fabric.exception;

public class InitializeCryptoSuiteException extends Exception {
    public InitializeCryptoSuiteException() {
    }

    public InitializeCryptoSuiteException(String message) {
        super(message);
    }

    public InitializeCryptoSuiteException(Throwable cause) {
        super(cause);
    }

    public InitializeCryptoSuiteException(String message, Throwable cause) {
        super(message, cause);
    }

}
