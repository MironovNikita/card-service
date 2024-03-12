package org.application.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CardDataEncryptorTest {
    @InjectMocks
    private CardDataEncryptor cardDataEncryptor;

    @Test
    @DisplayName("Проверка шифрования/дешифрования данных")
    void shouldCodeAndEncodeCorrectly() {
        String data = "12313154";
        String encryptedData = cardDataEncryptor.encryptData(data);

        assertNotNull(encryptedData);
        assertNotEquals(data, encryptedData);

        String decryptedData = cardDataEncryptor.decryptData(encryptedData);

        assertNotNull(decryptedData);
        assertEquals(data, decryptedData);
    }
}
