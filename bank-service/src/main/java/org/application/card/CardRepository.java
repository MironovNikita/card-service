package org.application.card;

import org.application.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByOwnerId(Long ownerId);

    Card findCardByNumber(String number);

    List<Card> findAllByExpirationDate(LocalDate expirationDate);
}
