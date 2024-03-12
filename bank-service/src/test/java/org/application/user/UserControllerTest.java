package org.application.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.TestUserData;
import org.application.common.exception.ObjectNotFoundException;
import org.application.user.entity.User;
import org.application.user.entity.UserDto;
import org.application.user.entity.UserSafeDto;
import org.application.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
public class UserControllerTest {
    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("Проверка метода на создание пользователя")
    void shouldCreateUser() throws Exception {
        Long id = 1L;
        UserDto userDto = TestUserData.createTestUserDto(id);
        UserSafeDto userSafeDto = TestUserData.createTestUserSafeDto(id);

        when(userService.create(userDto)).thenReturn(userSafeDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(userSafeDto)));

        verify(userService).create(userDto);
    }

    @Test
    @DisplayName("Проверка метода создания пользователя при уже занятом номере телефона")
    void shouldNotCreateUserIfPhoneNumberIsAlreadyRegistered() throws Exception {
        Long id = 1L;
        User user = TestUserData.createTestUser(id);
        UserDto userDto = TestUserData.createTestUserDto(id);
        userDto.setEmail("newemail@yandex.ru");

        when(userService.create(userDto)).thenThrow(new DataIntegrityViolationException(
                String.format("Пользователь с таким номером телефона (%s) уже существует", user.getPhone())));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(String.format(
                        "Пользователь с таким номером телефона (%s) уже существует", user.getPhone())))
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.time").exists());

        verify(userService).create(userDto);
    }

    @Test
    @DisplayName("Проверка метода создания пользователя при уже занятом email")
    void shouldNotCreateUserIfEmailIsAlreadyRegistered() throws Exception {
        Long id = 1L;
        User user = TestUserData.createTestUser(id);
        UserDto userDto = TestUserData.createTestUserDto(id);
        userDto.setPhone("89956552332");

        when(userService.create(userDto)).thenThrow(new DataIntegrityViolationException(
                String.format("Пользователь с таким email (%s) уже существует", user.getEmail())));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(String.format(
                        "Пользователь с таким email (%s) уже существует", user.getEmail())))
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.time").exists());

        verify(userService).create(userDto);
    }

    @Test
    @DisplayName("Проверка метода на создание пользователя с некорректными данными (Телефон не российского формата)")
    void shouldNotCreateUserWithIncorrectPhoneFormat() throws Exception {
        Long id = 1L;
        UserDto userDto = TestUserData.createTestUserDto(id);
        userDto.setPhone("12345678911");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.errors[0].field").value("phone"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Телефон должен начинаться с 7 или 8 и содержать только цифры"));

        verify(userService, never()).create(any(UserDto.class));
    }

    @Test
    @DisplayName("Проверка метода на создание пользователя с некорректными данными (Телефон не содержит 11 цифр)")
    void shouldNotCreateUserWithIncorrectPhoneSize() throws Exception {
        Long id = 1L;
        UserDto userDto = TestUserData.createTestUserDto(id);
        userDto.setPhone("823");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.errors[0].field").value("phone"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Телефонный номер должен состоять из 11 цифр"));

        verify(userService, never()).create(any(UserDto.class));
    }

    @Test
    @DisplayName("Проверка метода на создание пользователя с некорректными данными (Дата рождения из будущего)")
    void shouldNotCreateUserWithIncorrectBirthday() throws Exception {
        Long id = 1L;
        UserDto userDto = TestUserData.createTestUserDto(id);
        userDto.setBirthday(LocalDate.of(2500, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.errors[0].field").value("birthday"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Введена некорректная дата рождения"));

        verify(userService, never()).create(any(UserDto.class));
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя")
    void shouldUpdateUser() throws Exception {
        Long id = 1L;
        UserSafeDto userSafeDto = TestUserData.createTestUserSafeDto(id);
        UserDto userDto = new UserDto();
        userDto.setName("NewName");
        userDto.setEmail("newemail@mail.ru");

        when(userService.update(id, userDto)).thenReturn(userSafeDto);

        mockMvc.perform(patch(String.format("/users/%d", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userSafeDto)));

        verify(userService).update(id, userDto);
    }

    @Test
    @DisplayName("Проверка метода обновления несуществующего пользователя")
    void shouldThrowObjectNotFoundExceptionIfUpdatingNonExistentUser() throws Exception {
        Long id = 1L;
        UserDto userDto = new UserDto();
        userDto.setName("NewName");
        userDto.setEmail("newemail@mail.ru");

        when(userService.update(id, userDto)).thenThrow(new ObjectNotFoundException("Пользователь", id));

        mockMvc.perform(patch(String.format("/users/%d", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с ID: 1 не найден!"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.time").exists());

        verify(userService).update(id, userDto);
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя с некорректными данными (email)")
    void shouldNotUpdateUserWithIncorrectEmail() throws Exception {
        Long id = 1L;
        UserDto userDto = new UserDto();
        userDto.setEmail("incorrect");

        mockMvc.perform(patch(String.format("/users/%d", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("Введённый email некорректен"));

        verify(userService, never()).update(anyLong(), any(UserDto.class));
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя с некорректными данными (Слишком длинное имя)")
    void shouldNotUpdateUserWithIncorrectName() throws Exception {
        Long id = 1L;
        UserDto userDto = new UserDto();
        userDto.setName("Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy");

        mockMvc.perform(patch(String.format("/users/%d", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Размер имени не должен превышать 70 символов"));

        verify(userService, never()).update(anyLong(), any(UserDto.class));
    }

    @Test
    @DisplayName("Проверка метода на получение пользователя по ID")
    void shouldReturnUserSafeDtoById() throws Exception {
        Long id = 1L;
        UserSafeDto userSafeDto = TestUserData.createTestUserSafeDto(id);

        when(userService.get(id)).thenReturn(userSafeDto);

        mockMvc.perform(get(String.format("/users/%d", id)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userSafeDto)));

        verify(userService).get(id);
    }

    @Test
    @DisplayName("Проверка метода на получение пользователя по некорректному ID")
    void shouldReturnBadRequestIfGettingByIncorrectIdFormat() throws Exception {
        mockMvc.perform(get("/users/od")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Проверка метода на получение пользователя по несуществующему ID")
    void shouldReturnObjectNotFoundExceptionIfGettingByNonexistentId() throws Exception {
        Long id = 9999L;

        when(userService.get(id)).thenThrow(new ObjectNotFoundException("Пользователь", id));

        mockMvc.perform(get(String.format("/users/%d", id)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(String.format("Пользователь с ID: %d не найден!", id)))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.time").exists());

        verify(userService).get(id);
    }

    @Test
    @DisplayName("Проверка метода на удаление пользователя по ID")
    void shouldDeleteUserById() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete(String.format("/users/%d", id))).andExpect(status().isOk());
        verify(userService).delete(id);
    }

    @Test
    @DisplayName("Проверка метода удаления пользователя по несуществующему ID")
    void shouldThrowObjectNotFoundExceptionIfNonexistentId() throws Exception {
        Long id = 9999L;

        doThrow(new ObjectNotFoundException("Пользователь", id)).when(userService).delete(id);

        mockMvc.perform(delete(String.format("/users/%d", id)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(String.format("Пользователь с ID: %d не найден!", id)))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.time").exists());

        verify(userService).delete(id);
    }
}
