package org.application.card;

import org.application.TestCardData;
import org.application.TestUserData;
import org.application.card.entity.Card;
import org.application.card.entity.CardSafeDto;
import org.application.card.service.CardServiceImpl;
import org.application.client.WebClientService;
import org.application.common.entity.EmailStructure;
import org.application.common.exception.CardOwnershipException;
import org.application.common.exception.ObjectNotFoundException;
import org.application.common.security.CardDataEncryptor;
import org.application.common.security.CardDataGenerator;
import org.application.user.UserRepository;
import org.application.user.entity.User;
import org.application.user.entity.UserCardDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardDataGenerator cardDataGenerator;

    @Mock
    private CardDataEncryptor cardDataEncryptor;

    @Mock
    private WebClientService webClientService;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    @DisplayName("Проверка метода открытия карты")
    void shouldCreateCard() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        UserCardDto userCardDto = new UserCardDto(user.getName(), user.getSurname());

        Card card = TestCardData.createCardData(1L, user, "4395252563639696", "123");
        CardSafeDto cardSafeDto = TestCardData.createCardSafeDto(1L, userCardDto, card.getNumber());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardDataGenerator.generateCardNumber()).thenReturn(card.getNumber());
        when(cardDataEncryptor.encryptData(card.getNumber())).thenReturn(card.getNumber());
        when(cardDataGenerator.generateCardCVV()).thenReturn(card.getCvv());
        when(cardDataEncryptor.encryptData(card.getCvv())).thenReturn(card.getCvv());
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.mapCardToCardSafeDto(any(Card.class))).thenReturn(cardSafeDto);
        when(cardDataEncryptor.decryptData(card.getNumber())).thenReturn(card.getNumber());

        CardSafeDto result = cardService.open(userId);

        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor, times(2)).decryptData(card.getNumber());
        verify(webClientService).sendNotification(eq(user.getEmail()), any(EmailStructure.class));

        assertEquals(cardSafeDto, result);
    }

    @Test
    @DisplayName("Проверка метода получения всех карт пользователя")
    void shouldGetAllUserCards() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        Card card3 = TestCardData.createCardData(3L, user, "4395555522223333", "333");
        List<Card> expectedCardList = List.of(card1, card2, card3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(expectedCardList);
        when(cardMapper.mapCardToCardSafeDto(any(Card.class))).thenReturn(new CardSafeDto());
        when(cardDataEncryptor.decryptData(eq(null))).thenReturn(anyString());

        List<CardSafeDto> result = cardService.getAll(userId);

        assertNotNull(result);
        assertEquals(expectedCardList.size(), result.size());

        verify(cardMapper, times(3)).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor).decryptData(anyString());
    }

    @Test
    @DisplayName("Проверка метода получения всех карт несуществующего пользователя")
    void shouldThrowObjectNorFoundExceptionIfUserIsNonexistent() {
        Long userId = 9999L;

        when(userRepository.findById(userId)).thenThrow(new ObjectNotFoundException("Пользователь", userId));

        assertThatThrownBy(() -> cardService.getAll(userId)).isInstanceOf(ObjectNotFoundException.class);
        verify(cardRepository, never()).findAllByOwnerId(userId);
    }

    @Test
    @DisplayName("Проверка метода получения всех карт пользователя, у которого нет карт")
    void shouldReturnEmptyCardSafeDtoListIfUserHasNotAnyCards() {
        Long userId = 9999L;
        User user = TestUserData.createTestUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(Collections.emptyList());

        List<CardSafeDto> result = cardService.getAll(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Проверка метода получения открытых карт пользователя")
    void shouldGetAllOpenedUserCards() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        List<Card> expectedCardList = List.of(card1, card2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(expectedCardList);
        when(cardMapper.mapCardToCardSafeDto(any(Card.class))).thenReturn(new CardSafeDto());
        when(cardDataEncryptor.decryptData(eq(null))).thenReturn(anyString());

        List<CardSafeDto> result = cardService.getOpened(userId);

        assertNotNull(result);
        assertEquals(expectedCardList.size(), result.size());

        verify(cardMapper, times(2)).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor).decryptData(anyString());
    }

    @Test
    @DisplayName("Проверка метода получения открытых карт пользователя, у которого все карты закрыты")
    void shouldReturnEmptyListIfUserHasNoOpenedCards() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        card1.setOpened(false);
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        card2.setOpened(false);
        Card card3 = TestCardData.createCardData(3L, user, "4395555522223333", "333");
        card3.setOpened(false);
        List<Card> expectedCardList = List.of(card1, card2, card3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(expectedCardList);

        List<CardSafeDto> result = cardService.getOpened(userId);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(cardMapper, never()).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor, never()).decryptData(anyString());
    }

    @Test
    @DisplayName("Проверка метода получения всех открытых карт пользователя, у которого нет карт")
    void shouldReturnEmptyCardSafeDtoListIfUserHasNotAnyOpenedCards() {
        Long userId = 9999L;
        User user = TestUserData.createTestUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(Collections.emptyList());

        List<CardSafeDto> result = cardService.getOpened(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Проверка метода получения закрытых карт пользователя")
    void shouldGetAllClosedUserCards() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        card1.setOpened(false);
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        card2.setOpened(false);
        List<Card> expectedCardList = List.of(card1, card2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(expectedCardList);
        when(cardMapper.mapCardToCardSafeDto(any(Card.class))).thenReturn(new CardSafeDto());
        when(cardDataEncryptor.decryptData(eq(null))).thenReturn(anyString());

        List<CardSafeDto> result = cardService.getClosed(userId);

        assertNotNull(result);
        assertEquals(expectedCardList.size(), result.size());

        verify(cardMapper, times(2)).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor).decryptData(anyString());
    }

    @Test
    @DisplayName("Проверка метода получения закрытых карт пользователя, у которого все карты открыты")
    void shouldReturnEmptyListIfUserHasNoClosedCards() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Card card1 = TestCardData.createCardData(1L, user, "4395252563639696", "111");
        Card card2 = TestCardData.createCardData(2L, user, "4395789456123654", "222");
        Card card3 = TestCardData.createCardData(3L, user, "4395555522223333", "333");

        List<Card> expectedCardList = List.of(card1, card2, card3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(expectedCardList);

        List<CardSafeDto> result = cardService.getClosed(userId);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(cardMapper, never()).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor, never()).decryptData(anyString());
    }

    @Test
    @DisplayName("Проверка метода получения всех закрытых карт пользователя, у которого нет карт")
    void shouldReturnEmptyCardSafeDtoListIfUserHasNotAnyClosedCards() {
        Long userId = 9999L;
        User user = TestUserData.createTestUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByOwnerId(userId)).thenReturn(Collections.emptyList());

        List<CardSafeDto> result = cardService.getClosed(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Проверка метода закрытия карты")
    void shouldCloseCard() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Long cardId = 1L;
        Card card = TestCardData.createCardData(cardId, user, "4395252563639696", "111");
        CardSafeDto cardSafeDto = TestCardData.createCardSafeDto(cardId, new UserCardDto(user.getName(),
                user.getSurname()), card.getNumber());
        cardSafeDto.setOpened(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.mapCardToCardSafeDto(any(Card.class))).thenReturn(cardSafeDto);
        when(cardDataEncryptor.decryptData(card.getNumber())).thenReturn(card.getNumber());

        CardSafeDto result = cardService.close(userId, cardId);

        assertNotNull(result);
        assertFalse(result.getOpened());

        verify(cardMapper).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor, times(2)).decryptData(card.getNumber());
        verify(webClientService).sendNotification(eq(user.getEmail()), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода закрытия карты при несуществующем пользователе")
    void shouldNotCloseCardIfNonexistentUser() {
        Long userId = 1L;
        Long cardId = 1L;

        when(userRepository.findById(userId)).thenThrow(new ObjectNotFoundException("Пользователь", userId));

        assertThatThrownBy(() -> cardService.close(userId, cardId)).isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(cardRepository, never()).findById(cardId);
        verify(cardMapper, never()).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor, never()).decryptData(anyString());
        verify(webClientService, never()).sendNotification(anyString(), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода закрытия карты при несуществующей карте")
    void shouldNotCloseCardIfNonexistentCard() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        Long cardId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenThrow(new ObjectNotFoundException("Банковская карта", cardId));

        assertThatThrownBy(() -> cardService.close(userId, cardId)).isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(cardRepository).findById(cardId);
        verify(cardMapper, never()).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor, never()).decryptData(anyString());
        verify(webClientService, never()).sendNotification(eq(user.getEmail()), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода закрытия карты при условии, что пользователь не является её собственником")
    void shouldNotCloseCardIfUserIsNotItsOwner() {
        Long notOwnerId = 1L;
        User notOwner = TestUserData.createTestUser(notOwnerId);
        User user = TestUserData.createTestUser(2L);
        Long cardId = 1L;

        Card card = TestCardData.createCardData(cardId, user, "4395252563639696", "111");

        when(userRepository.findById(notOwnerId)).thenReturn(Optional.of(notOwner));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.close(notOwnerId, cardId)).isInstanceOf(CardOwnershipException.class);

        verify(userRepository).findById(notOwnerId);
        verify(cardRepository).findById(cardId);
        verify(cardMapper, never()).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor, never()).decryptData(anyString());
        verify(webClientService, never()).sendNotification(anyString(), any(EmailStructure.class));
        verify(webClientService, never()).sendNotification(eq(user.getEmail()), any(EmailStructure.class));
        verify(webClientService, never()).sendNotification(eq(notOwner.getEmail()), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода закрытия карты при условии, что она уже закрыта")
    void shouldReturnCardSafeDtoIfCardHasAlreadyBeenClosed() {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);

        Long cardId = 1L;
        Card card = TestCardData.createCardData(cardId, user, "4395252563639696", "111");
        card.setOpened(false);
        CardSafeDto cardSafeDto = TestCardData.createCardSafeDto(cardId, new UserCardDto(user.getName(),
                user.getSurname()), card.getNumber());
        cardSafeDto.setOpened(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.mapCardToCardSafeDto(any(Card.class))).thenReturn(cardSafeDto);
        when(cardDataEncryptor.decryptData(card.getNumber())).thenReturn(card.getNumber());

        CardSafeDto result = cardService.close(userId, cardId);

        assertNotNull(result);
        assertFalse(result.getOpened());

        verify(cardMapper).mapCardToCardSafeDto(any(Card.class));
        verify(cardDataEncryptor).decryptData(card.getNumber());
        verify(webClientService, never()).sendNotification(eq(user.getEmail()), any(EmailStructure.class));
    }
}
