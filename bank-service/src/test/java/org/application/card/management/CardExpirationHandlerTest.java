package org.application.card.management;

import org.application.TestCardData;
import org.application.TestUserData;
import org.application.card.CardRepository;
import org.application.card.entity.Card;
import org.application.card.entity.CardSafeDto;
import org.application.card.service.CardService;
import org.application.client.WebClientService;
import org.application.common.entity.EmailStructure;
import org.application.common.security.CardDataEncryptor;
import org.application.user.entity.User;
import org.application.user.entity.UserCardDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardExpirationHandlerTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private WebClientService webClientService;

    @Mock
    private CardDataEncryptor cardDataEncryptor;

    @Mock
    private CardService cardService;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @InjectMocks
    private CardExpirationHandler cardExpirationHandler;

    @Test
    @DisplayName("Проверка метода проверки срока действия карт")
    void shouldReturnCardExpirationListIfThereAreSomeExpired() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Card card = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        card.setExpirationDate(LocalDate.of(2024, 1, 1));
        List<Card> expiringCards = List.of(card);

        when(cardRepository.findAllByExpirationDate(any(LocalDate.class))).thenReturn(expiringCards);
        when(cardDataEncryptor.decryptData(card.getNumber())).thenReturn(card.getNumber());

        cardExpirationHandler.checkCardExpiration();

        verify(cardRepository).findAllByExpirationDate(any(LocalDate.class));
        verify(webClientService).sendNotification(emailCaptor.capture(), any(EmailStructure.class));

        List<String> capturedEmails = emailCaptor.getAllValues();
        assertEquals(1, capturedEmails.size());
        assertEquals(user.getEmail(), capturedEmails.getFirst());
    }

    @Test
    @DisplayName("Проверка метода проверки срока действия карты и её перевыпуска")
    void shouldReturnCardExpirationListAndCloseCardsAndReissueCardsIfThereAreSomeExpired() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Card card = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        card.setExpirationDate(LocalDate.of(2024, 1, 1));
        List<Card> expiringCards = List.of(card);

        CardSafeDto cardSafeDto = TestCardData.createCardSafeDto(1L, new UserCardDto(user.getName(),
                user.getSurname()), "4395789456123654");

        when(cardRepository.findAllByExpirationDate(any(LocalDate.class))).thenReturn(expiringCards);
        when(cardService.open(userId)).thenReturn(cardSafeDto);
        when(cardDataEncryptor.decryptData(card.getNumber())).thenReturn(card.getNumber());

        cardExpirationHandler.reissueCard();

        verify(cardRepository).findAllByExpirationDate(any(LocalDate.class));
        verify(cardService).open(userId);
        verify(webClientService).sendNotification(emailCaptor.capture(), any(EmailStructure.class));

        List<String> capturedEmails = emailCaptor.getAllValues();
        assertEquals(1, capturedEmails.size());
        assertEquals(user.getEmail(), capturedEmails.getFirst());
    }
}