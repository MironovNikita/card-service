package org.application.common.exception;

public class DecryptionException extends RuntimeException {
    public DecryptionException(String message, Throwable cause) {
        super(String.format("Ошибка дешифрования данных: %s", message), cause);
    }
}
