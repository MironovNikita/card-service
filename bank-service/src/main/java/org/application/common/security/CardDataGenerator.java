package org.application.common.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardDataGenerator {
    private static final String BIN = "4395";
    private static final int NUM_LENGTH = 16;

    public String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder numberBuilder = new StringBuilder(BIN);

        for (int i = 0; i < NUM_LENGTH - BIN.length() - 1; i++) {
            numberBuilder.append(random.nextInt(10));
        }

        //Контрольная сумма по алгоритму Луна
        String partialNum = numberBuilder.toString();
        int sum = 0;
        boolean alternate = false;
        for (int i = partialNum.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(partialNum.substring(i, i + 1));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        int checkSum = (sum * 9) % 10;

        return partialNum + checkSum;
    }

    public String generateCardCVV() {
        SecureRandom random = new SecureRandom();
        StringBuilder cvvBuilder = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            cvvBuilder.append(random.nextInt(10));
        }
        return cvvBuilder.toString();
    }
}
