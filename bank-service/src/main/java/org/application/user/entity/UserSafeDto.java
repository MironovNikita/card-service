package org.application.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.application.common.constants.BCConstants;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class UserSafeDto {
    private Long id;

    private String surname;

    private String name;

    private String patronymic;

    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BCConstants.DATE_FORMAT)
    private LocalDate birthday;

    private String phone;
}
