package dev.bacteriawa.mint.exception;

public class MintRuntimeException extends RuntimeException {
    public MintRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MintRuntimeException(String message) {
        super(message);
    }

    public MintRuntimeException(Throwable cause) {
        super(cause);
    }
}
