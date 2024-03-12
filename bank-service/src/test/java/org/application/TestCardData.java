package org.application;

import org.application.card.entity.Card;
import org.application.card.entity.CardSafeDto;
import org.application.user.entity.User;
import org.application.user.entity.UserCardDto;

import java.time.LocalDate;

public class TestCardData {
    public static Card createCardData(Long id, User user, String number, String cvv) {
        return Card.builder()
                .id(id)
                .owner(user)
                .number(number)
                .issueDate(LocalDate.of(2024, 1, 1))
                .expirationDate(LocalDate.of(2027, 1, 1))
                .cvv(cvv)
                .opened(true)
                .build();
    }

    public static CardSafeDto createCardSafeDto(Long id, UserCardDto userCardDto, String number) {
        return CardSafeDto.builder()
                .id(id)
                .owner(userCardDto)
                .number(number)
                .expirationDate(LocalDate.of(2027, 1, 1))
                .opened(true)
                .build();
    }
}
