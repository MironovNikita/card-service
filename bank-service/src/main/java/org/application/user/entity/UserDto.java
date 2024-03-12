package org.application.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import org.application.common.constants.BCConstants;
import org.application.common.validation.Create;
import org.application.common.validation.Phone;
import org.application.common.validation.Update;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class UserDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Фамилия должна быть заполнена")
    @Size(groups = {Create.class, Update.class}, max = 70, message = "Размер фамилии не должен превышать 70 символов")
    private String surname;

    @NotBlank(groups = Create.class, message = "Имя должно быть заполнено")
    @Size(groups = {Create.class, Update.class}, max = 70, message = "Размер имени не должен превышать 70 символов")
    private String name;

    @NotBlank(groups = Create.class, message = "Отчество должно быть заполнено")
    @Size(groups = {Create.class, Update.class}, max = 70, message = "Размер отчества не должен превышать 70 символов")
    private String patronymic;

    @NotBlank(groups = Create.class, message = "Email пользователя не может быть пустым")
    @Size(groups = {Create.class, Update.class}, min = 5, max = 50,
            message = "Размер email должен быть от 5 до 50 символов")
    @Email(groups = {Create.class, Update.class}, message = "Введённый email некорректен")
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BCConstants.DATE_FORMAT)
    @NotNull(groups = Create.class, message = "Дата рождения клиента обязательна")
    @Past(groups = {Create.class, Update.class}, message = "Введена некорректная дата рождения")
    private LocalDate birthday;

    @Phone(groups = {Create.class, Update.class})
    @NotBlank(groups = Create.class, message = "Телефон должен быть заполнен")
    @Size(groups = {Create.class, Update.class}, min = 11, max = 11,
            message = "Телефонный номер должен состоять из 11 цифр")
    private String phone;

    @NotBlank(groups = Create.class, message = "Пароль должен быть заполнен")
    @Size(groups = {Create.class, Update.class}, min = 10, max = 20,
            message = "Размер пароля должен быть от 10 до 20 символов")
    private String password;
}