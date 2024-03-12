package org.application.common.security;

import lombok.extern.slf4j.Slf4j;
import org.application.common.exception.DecryptionException;
import org.application.common.exception.EncryptionException;
import org.application.common.exception.KeyGenerationException;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class CardDataEncryptor {
    private static final String AES = "AES";
    private static final Path KEY_FILE_PATH = Paths.get("secret.key");
    private static SecretKeySpec secretKeySpec;

    static {
        loadOrGenerateKey();
    }

    private static void loadOrGenerateKey() {
        if (Files.exists(KEY_FILE_PATH)) {
            try {
                byte[] encodedKey = Files.readAllBytes(KEY_FILE_PATH);
                secretKeySpec = new SecretKeySpec(encodedKey, AES);
            } catch (IOException exception) {
                log.error("Ошибка при чтении ключа из файла: {}", exception.getMessage());
                throw new KeyGenerationException(exception.getMessage(), exception);
            }
        } else {
            try {
                generateAndSaveSecretKey();
            } catch (NoSuchAlgorithmException | IOException exception) {
                throw new KeyGenerationException(exception.getMessage(), exception);
            }
        }
    }

    private static void generateAndSaveSecretKey() throws NoSuchAlgorithmException, IOException {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
        keyGenerator.init(256, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), AES);

        Files.write(KEY_FILE_PATH, secretKeySpec.getEncoded());
    }

    public String encryptData(String data) {
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException exception) {
            throw new EncryptionException(exception.getMessage(), exception);
        }
    }

    public String decryptData(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedData);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException exception) {
            throw new DecryptionException(exception.getMessage(), exception);
        }
    }
}
