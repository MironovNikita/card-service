package org.application.common.exception;

public class CardOwnershipException extends RuntimeException {
    public CardOwnershipException(Long userId, Long cardId) {
        super(String.format("Невозможно выполнить запрос по закрытию карты! Карта с ID %d не принадлежит " +
                "пользователю с ID %d!", cardId, userId));
    }
}
