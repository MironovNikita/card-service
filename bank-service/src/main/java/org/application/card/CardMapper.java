package org.application.card;

import org.application.card.entity.Card;
import org.application.card.entity.CardSafeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardSafeDto mapCardToCardSafeDto(Card card);
}
