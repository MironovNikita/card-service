package org.application.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.card.entity.CardSafeDto;
import org.application.card.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/users/{userId}/cards")
public class CardController {
    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardSafeDto open(@PathVariable Long userId) {
        log.info("CardController: Запрос на открытие банковской карты пользователем с ID {}", userId);
        return cardService.open(userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CardSafeDto> getAll(@PathVariable Long userId) {
        log.info("CardController: Запрос на получение всех банковских карт пользователем с ID {}", userId);
        return cardService.getAll(userId);
    }

    @GetMapping("/opened")
    @ResponseStatus(HttpStatus.OK)
    public List<CardSafeDto> getOpened(@PathVariable Long userId) {
        log.info("CardController: Запрос на получение всех открытых банковских карт пользователем с ID {}", userId);
        return cardService.getOpened(userId);
    }

    @GetMapping("/closed")
    @ResponseStatus(HttpStatus.OK)
    public List<CardSafeDto> getClosed(@PathVariable Long userId) {
        log.info("CardController: Запрос на получение всех закрытых банковских карт пользователем с ID {}", userId);
        return cardService.getClosed(userId);
    }

    @PatchMapping("/{cardId}")
    @ResponseStatus(HttpStatus.OK)
    public CardSafeDto close(@PathVariable Long userId,
                             @PathVariable Long cardId) {
        log.info("CardController: Запрос на закрытие банковской карты с ID {} пользователем с ID {}", cardId, userId);
        return cardService.close(userId, cardId);
    }
}
