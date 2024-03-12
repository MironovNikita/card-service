package org.application.card.entity;

import lombok.*;
import org.application.user.entity.UserCardDto;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CardSafeDto {
    private Long id;

    private UserCardDto owner;

    private String number;

    private LocalDate expirationDate;

    private Boolean opened;
}
