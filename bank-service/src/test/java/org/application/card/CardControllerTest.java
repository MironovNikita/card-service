package org.application.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.TestCardData;
import org.application.TestUserData;
import org.application.card.entity.CardSafeDto;
import org.application.card.service.CardService;
import org.application.user.entity.User;
import org.application.user.entity.UserCardDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
public class CardControllerTest {
    @MockBean
    private CardService cardService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private CardController cardController;

    @Test
    @DisplayName("Проверка метода открытия карты")
    void shouldCreateCard() throws Exception {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        CardSafeDto cardSafeDto = TestCardData.createCardSafeDto(1L, new UserCardDto(user.getName(),
                user.getSurname()), "4395252563639696");

        when(cardService.open(userId)).thenReturn(cardSafeDto);

        mockMvc.perform(post(String.format("/users/%d/cards", userId)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(cardSafeDto)));

        verify(cardService).open(userId);
    }

    @Test
    @DisplayName("Проверка метода открытия карты при некорректном ID")
    void shouldReturnBadRequestIfOpeningCardByIncorrectIdFormat() throws Exception {
        mockMvc.perform(post("/users/wantCard/cards")).andExpect(status().isBadRequest());

        verify(cardService, never()).open(anyLong());
    }

    @Test
    @DisplayName("Проверка метода получения всех карт пользователя")
    void shouldReturnAllUserCards() throws Exception {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        CardSafeDto cardSafeDto1 = TestCardData.createCardSafeDto(1L, new UserCardDto(user.getName(),
                user.getSurname()), "4395252563639696");
        CardSafeDto cardSafeDto2 = TestCardData.createCardSafeDto(2L, new UserCardDto(user.getName(),
                user.getSurname()), "4395789456123654");
        CardSafeDto cardSafeDto3 = TestCardData.createCardSafeDto(3L, new UserCardDto(user.getName(),
                user.getSurname()), "4395555522223333");
        List<CardSafeDto> expectedList = List.of(cardSafeDto1, cardSafeDto2, cardSafeDto3);

        when(cardService.getAll(userId)).thenReturn(expectedList);

        mockMvc.perform(get(String.format("/users/%d/cards", userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedList)));

        verify(cardService).getAll(userId);
    }

    @Test
    @DisplayName("Проверка метода получения всех карт пользователя при некорректном ID")
    void shouldReturnBadRequestIfGettingAllCardsByIncorrectUserIdFormat() throws Exception {
        mockMvc.perform(get("/users/getCard/cards")).andExpect(status().isBadRequest());

        verify(cardService, never()).getAll(anyLong());
    }

    @Test
    @DisplayName("Проверка метода получения всех открытых карт пользователя")
    void shouldReturnAllOpenedUserCards() throws Exception {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        CardSafeDto cardSafeDto1 = TestCardData.createCardSafeDto(1L, new UserCardDto(user.getName(),
                user.getSurname()), "4395252563639696");
        CardSafeDto cardSafeDto2 = TestCardData.createCardSafeDto(2L, new UserCardDto(user.getName(),
                user.getSurname()), "4395789456123654");
        CardSafeDto cardSafeDto3 = TestCardData.createCardSafeDto(3L, new UserCardDto(user.getName(),
                user.getSurname()), "4395555522223333");
        cardSafeDto3.setOpened(false);
        List<CardSafeDto> expectedList = List.of(cardSafeDto1, cardSafeDto2);

        when(cardService.getOpened(userId)).thenReturn(expectedList);

        mockMvc.perform(get(String.format("/users/%d/cards/opened", userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedList)));

        verify(cardService).getOpened(userId);
    }

    @Test
    @DisplayName("Проверка метода получения всех открытых карт пользователя при некорректном ID")
    void shouldReturnBadRequestIfGettingAllOpenedCardsByIncorrectUserIdFormat() throws Exception {
        mockMvc.perform(get("/users/getOpenedCards/cards/opened")).andExpect(status().isBadRequest());

        verify(cardService, never()).getOpened(anyLong());
    }

    @Test
    @DisplayName("Проверка метода получения всех закрытых карт пользователя")
    void shouldReturnAllClosedUserCards() throws Exception {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        CardSafeDto cardSafeDto1 = TestCardData.createCardSafeDto(1L, new UserCardDto(user.getName(),
                user.getSurname()), "4395252563639696");
        cardSafeDto1.setOpened(false);
        CardSafeDto cardSafeDto2 = TestCardData.createCardSafeDto(2L, new UserCardDto(user.getName(),
                user.getSurname()), "4395789456123654");
        cardSafeDto2.setOpened(false);
        List<CardSafeDto> expectedList = List.of(cardSafeDto1, cardSafeDto2);

        when(cardService.getClosed(userId)).thenReturn(expectedList);

        mockMvc.perform(get(String.format("/users/%d/cards/closed", userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedList)));

        verify(cardService).getClosed(userId);
    }

    @Test
    @DisplayName("Проверка метода получения всех закрытых карт пользователя при некорректном ID")
    void shouldReturnBadRequestIfGettingAllClosedCardsByIncorrectUserIdFormat() throws Exception {
        mockMvc.perform(get("/users/getClosedCards/cards/closed")).andExpect(status().isBadRequest());

        verify(cardService, never()).getClosed(anyLong());
    }

    @Test
    @DisplayName("Проверка метода закрытия карты")
    void shouldCloseCard() throws Exception {
        Long userId = 1L;
        User user = TestUserData.createTestUser(userId);
        Long cardId = 1L;
        CardSafeDto cardSafeDto = TestCardData.createCardSafeDto(cardId, new UserCardDto(user.getName(),
                user.getSurname()), "4395252563639696");
        cardSafeDto.setOpened(false);

        when(cardService.close(userId, cardId)).thenReturn(cardSafeDto);

        mockMvc.perform(patch(String.format("/users/%d/cards/%d", userId, cardId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cardSafeDto)));

        verify(cardService).close(userId, cardId);
    }

    @Test
    @DisplayName("Проверка метода закрытия карты пользователя при некорректном ID пользователя")
    void shouldReturnBadRequestIfClosingUserCardByIncorrectUserIdFormat() throws Exception {
        mockMvc.perform(patch("/users/dsadasd/cards/1")).andExpect(status().isBadRequest());

        verify(cardService, never()).close(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Проверка метода закрытия карты пользователя при некорректном ID карты")
    void shouldReturnBadRequestIfClosingUserCardByIncorrectCardIdFormat() throws Exception {
        mockMvc.perform(patch("/users/1/cards/dasdasd")).andExpect(status().isBadRequest());

        verify(cardService, never()).close(anyLong(), anyLong());
    }
}
