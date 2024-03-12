package org.application.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.card.CardMapper;
import org.application.card.CardRepository;
import org.application.card.entity.Card;
import org.application.card.entity.CardSafeDto;
import org.application.client.WebClientService;
import org.application.common.constants.EmailMessages;
import org.application.common.entity.EmailStructure;
import org.application.common.exception.CardOwnershipException;
import org.application.common.exception.ObjectNotFoundException;
import org.application.common.security.CardDataEncryptor;
import org.application.common.security.CardDataGenerator;
import org.application.user.UserRepository;
import org.application.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardDataGenerator cardDataGenerator;
    private final CardDataEncryptor cardDataEncryptor;
    private final WebClientService webClientService;

    @Override
    @Transactional
    public CardSafeDto open(Long userId) {
        User user = checkUserExistence(userId);
        LocalDate date = LocalDate.now();

        Card card = Card.builder()
                .owner(user)
                .number(generateValidNumber())
                .issueDate(date)
                .expirationDate(date.plusYears(3))
                .cvv(cardDataEncryptor.encryptData(cardDataGenerator.generateCardCVV()))
                .opened(true)
                .build();

        cardRepository.save(card);

        sendNotification(user.getEmail(), EmailMessages.OPENED_CARD_SUBJECT,
                EmailMessages.openCard(user.getName(), user.getPatronymic(),
                        cardDataEncryptor.decryptData(card.getNumber())));

        CardSafeDto cardSafeDto = cardMapper.mapCardToCardSafeDto(card);
        cardSafeDto.setNumber(cardDataEncryptor.decryptData(card.getNumber()));

        return cardSafeDto;
    }

    @Override
    public List<CardSafeDto> getAll(Long userId) {
        checkUserExistence(userId);

        List<Card> userCards = cardRepository.findAllByOwnerId(userId);

        return userCards.stream()
                .map(cardMapper::mapCardToCardSafeDto)
                .peek(cardSafeDto -> cardSafeDto.setNumber(cardDataEncryptor.decryptData(cardSafeDto.getNumber())))
                .toList();
    }

    @Override
    public List<CardSafeDto> getOpened(Long userId) {
        checkUserExistence(userId);

        List<Card> userCards = cardRepository.findAllByOwnerId(userId);

        return userCards.stream()
                .filter(Card::getOpened)
                .map(cardMapper::mapCardToCardSafeDto)
                .peek(cardSafeDto -> cardSafeDto.setNumber(cardDataEncryptor.decryptData(cardSafeDto.getNumber())))
                .toList();
    }

    @Override
    public List<CardSafeDto> getClosed(Long userId) {
        checkUserExistence(userId);

        List<Card> userCards = cardRepository.findAllByOwnerId(userId);

        return userCards.stream()
                .filter(card -> !card.getOpened())
                .map(cardMapper::mapCardToCardSafeDto)
                .peek(cardSafeDto -> cardSafeDto.setNumber(cardDataEncryptor.decryptData(cardSafeDto.getNumber())))
                .toList();
    }

    @Override
    @Transactional
    public CardSafeDto close(Long userId, Long cardId) {
        User user = checkUserExistence(userId);

        Card card = checkCardExistenceById(cardId);

        if (!isOwner(user, card)) {
            throw new CardOwnershipException(userId, cardId);
        }

        if (card.getOpened()) {
            card.setOpened(false);
            sendNotification(user.getEmail(), EmailMessages.CLOSED_CARD_SUBJECT,
                    EmailMessages.closeCard(user.getName(), user.getPatronymic(),
                            cardDataEncryptor.decryptData(card.getNumber())));
        }

        CardSafeDto cardSafeDto = cardMapper.mapCardToCardSafeDto(card);
        cardSafeDto.setNumber(cardDataEncryptor.decryptData(cardSafeDto.getNumber()));

        return cardSafeDto;
    }

    private User checkUserExistence(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("Ошибка получения пользователя по ID {}, такого ID не существует!", userId);
            return new ObjectNotFoundException("Пользователь", userId);
        });
    }

    private Card checkCardExistenceById(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(() -> {
            log.error("Ошибка получения карты по ID {}, такого ID не существует!", cardId);
            return new ObjectNotFoundException("Банковская карта", cardId);
        });
    }

    private String generateValidNumber() {
        String number = generateAndEncryptNumber();
        while (cardRepository.findCardByNumber(number) != null) {
            number = generateAndEncryptNumber();
        }
        return number;
    }

    private String generateAndEncryptNumber() {
        return cardDataEncryptor.encryptData(cardDataGenerator.generateCardNumber());
    }

    private boolean isOwner(User user, Card card) {
        return card.getOwner().equals(user);
    }

    private void sendNotification(String email, String subject, String message) {
        EmailStructure emailStructure = new EmailStructure(subject, message);

        webClientService.sendNotification(email, emailStructure);
    }
}
