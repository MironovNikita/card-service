package org.application.common.exception;

public class EncryptionException extends RuntimeException {
    public EncryptionException(String message, Throwable cause) {
        super(String.format("Ошибка шифрования данных: %s", message), cause);
    }
}
