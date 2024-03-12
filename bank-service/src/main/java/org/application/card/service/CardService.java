package org.application.card.service;

import org.application.card.entity.CardSafeDto;

import java.util.List;

public interface CardService {
    CardSafeDto open(Long userId);

    List<CardSafeDto> getAll(Long userId);

    List<CardSafeDto> getOpened(Long userId);

    List<CardSafeDto> getClosed(Long userId);

    CardSafeDto close(Long userId, Long cardId);
}
