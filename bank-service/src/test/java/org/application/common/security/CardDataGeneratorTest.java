package org.application.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CardDataGeneratorTest {
    private CardDataGenerator cardDataGenerator;

    @BeforeEach
    void initialize() {
        cardDataGenerator = new CardDataGenerator();
    }

    @Test
    @DisplayName("Проверка метода генерирования номера карты")
    void shouldGenerateCorrectCardNumber() {
        String cardNumber = cardDataGenerator.generateCardNumber();

        assertEquals(16, cardNumber.length());
        assertTrue(cardNumber.startsWith("4395"));
        assertTrue(cardNumber.matches("\\d+"));

        Set<String> generatedNumbers = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String newCardNumber = cardDataGenerator.generateCardNumber();
            assertFalse(generatedNumbers.contains(newCardNumber));
            generatedNumbers.add(newCardNumber);
        }
    }

    @Test
    @DisplayName("Проверка метода генерирования CVV карты")
    void shouldGenerateCorrectCVV() {
        String cvv = cardDataGenerator.generateCardCVV();

        assertEquals(3, cvv.length());
        assertTrue(cvv.matches("\\d+"));
    }
}
