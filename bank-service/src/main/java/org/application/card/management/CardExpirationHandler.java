package org.application.card.management;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.card.CardRepository;
import org.application.card.entity.Card;
import org.application.card.entity.CardSafeDto;
import org.application.card.service.CardService;
import org.application.client.WebClientService;
import org.application.common.constants.EmailMessages;
import org.application.common.entity.EmailStructure;
import org.application.common.security.CardDataEncryptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardExpirationHandler {
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final WebClientService webClientService;
    private final CardDataEncryptor cardDataEncryptor;

    //- для ручного тестирования
    //@Scheduled(cron = "0 10 16 * * *")
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional(readOnly = true)
    public void checkCardExpiration() {
        LocalDate expirationDate = LocalDate.now().plusDays(7);

        List<Card> expiringCards = cardRepository.findAllByExpirationDate(expirationDate);

        if (!expiringCards.isEmpty()) {
            for (Card card : expiringCards) {
                String email = card.getOwner().getEmail();
                String name = card.getOwner().getName();
                String patronymic = card.getOwner().getPatronymic();
                String cardNumber = cardDataEncryptor.decryptData(card.getNumber());

                webClientService.sendNotification(email,
                        new EmailStructure(EmailMessages.CARD_EXPIRATION_SUBJECT,
                                EmailMessages.notifyExpiration(name, patronymic, cardNumber)));
            }
            log.info("Все уведомления о завершающихся сроках карт были успешно отправлены!");
        }
    }

    //- для ручного тестирования
    //@Scheduled(cron = "0 10 16 * * *")
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void reissueCard() {
        LocalDate expirationDate = LocalDate.now();

        List<Card> expiringCards = cardRepository.findAllByExpirationDate(expirationDate);

        if (!expiringCards.isEmpty()) {
            for (Card card : expiringCards) {
                if (card.getOpened()) {
                    card.setOpened(false);

                    CardSafeDto cardSafeDto = cardService.open(card.getOwner().getId());

                    String name = card.getOwner().getName();
                    String patronymic = card.getOwner().getPatronymic();
                    String email = card.getOwner().getEmail();
                    String oldCardNumber = cardDataEncryptor.decryptData(card.getNumber());
                    String newCardNumber = cardSafeDto.getNumber();

                    webClientService.sendNotification(email,
                            new EmailStructure(EmailMessages.CARD_REISSUE_SUBJECT,
                                    EmailMessages.openNewProduct(name, patronymic, oldCardNumber, newCardNumber)));
                }
            }
            log.info("Все уведомления о перевыпуске карт были успешно отправлены!");
        }
    }
}
