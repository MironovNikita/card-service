package org.application.common.exception;

public class KeyGenerationException extends RuntimeException {
    public KeyGenerationException(String message, Throwable cause) {
        super(String.format("Ошибка создания секретного кода шифрования: %s", message), cause);
    }
}
