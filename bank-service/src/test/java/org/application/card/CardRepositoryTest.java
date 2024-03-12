package org.application.card;

import org.application.TestCardData;
import org.application.TestUserData;
import org.application.card.entity.Card;
import org.application.user.UserRepository;
import org.application.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CardRepositoryTest {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void deleteAll() {
        cardRepository.deleteAll();
    }

    @Test
    @DisplayName("Проверка метода получения всех карт по ID пользователя")
    void shouldFindAllUserCardsInRepository() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        userRepository.save(user);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        Card card3 = TestCardData.createCardData(3L, user, "4395555522223333", "333");
        List<Card> expectedCardList = List.of(card1, card2, card3);

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        assertEquals(cardRepository.findAllByOwnerId(userId), expectedCardList);
    }

    @Test
    @DisplayName("Проверка метода получения всех карт по несуществующему ID пользователя")
    void shouldNotFindAllUserCardsInRepositoryByNonexistentId() {
        assertTrue(cardRepository.findAllByOwnerId(9999L).isEmpty());
    }

    @Test
    @DisplayName("Проверка метода получения карты по номеру")
    void shouldFindCardByItsNumber() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        userRepository.save(user);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        Card card3 = TestCardData.createCardData(3L, user, "4395555522223333", "333");

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        assertEquals(cardRepository.findCardByNumber("4395252563639696"), card1);
        assertEquals(cardRepository.findCardByNumber("4395789456123654"), card2);
        assertEquals(cardRepository.findCardByNumber("4395555522223333"), card3);
    }

    @Test
    @DisplayName("Проверка метода получения карты по несуществующему номеру")
    void shouldNotFindCardByItsNonexistentNumber() {
        assertNull(cardRepository.findCardByNumber("4395555522223333"));
    }


    @Test
    @DisplayName("Проверка метода получения всех карт с истекающим сроком действия")
    void shouldReturnCardListWithExpirationDateOfSevenDaysUntilLocalDate() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        userRepository.save(user);

        LocalDate localDate = LocalDate.of(2024, 1, 1);
        LocalDate expirationDate = localDate.plusDays(7);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        card1.setExpirationDate(expirationDate);
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        card2.setExpirationDate(expirationDate);
        Card card3 = TestCardData.createCardData(3L, user, "4395555522223333", "333");

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        List<Card> expectedList = List.of(card1, card2);

        assertEquals(expectedList, cardRepository.findAllByExpirationDate(expirationDate));
    }
}
