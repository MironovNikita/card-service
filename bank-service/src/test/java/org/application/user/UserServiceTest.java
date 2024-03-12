package org.application.user;

import org.application.TestUserData;
import org.application.client.WebClientService;
import org.application.common.entity.EmailStructure;
import org.application.common.exception.ObjectNotFoundException;
import org.application.common.security.PasswordHandler;
import org.application.user.entity.User;
import org.application.user.entity.UserDto;
import org.application.user.entity.UserSafeDto;
import org.application.user.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordHandler passwordHandler;

    @Mock
    private WebClientService webClientService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Проверка метода создания пользователя")
    void shouldCreateUser() {
        UserDto userDto = TestUserData.createTestUserDto(1L);

        User user = new User();
        UserSafeDto userSafeDto = new UserSafeDto();

        when(passwordHandler.encodePassword(anyString())).thenReturn("encodedPassword");
        when(userMapper.mapUserDtoToUser(userDto)).thenReturn(user);
        when(userMapper.mapUserToUserSafeDto(user)).thenReturn(userSafeDto);
        when(userRepository.save(user)).thenReturn(user);

        UserSafeDto resultData = userService.create(userDto);

        verify(userRepository).save(user);
        verify(webClientService).sendNotification(eq(user.getEmail()), any(EmailStructure.class));
        assertEquals(userSafeDto, resultData);
    }

    @Test
    @DisplayName("Проверка метода создания пользователя при дублировании email")
    void shouldThrowDataIntegrityViolationExceptionIfEmailAlreadyExistsWhenCreating() {
        UserDto userDto = TestUserData.createTestUserDto(1L);

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        verify(userRepository, never()).save(any(User.class));
        verify(webClientService, never()).sendNotification(anyString(), any(EmailStructure.class));
        assertThatThrownBy(() -> userService.create(userDto)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Проверка метода создания пользователя при дублировании номера телефона")
    void shouldThrowDataIntegrityViolationExceptionIfPhoneNumberAlreadyExistsWhenCreating() {
        UserDto userDto = TestUserData.createTestUserDto(1L);

        when(userRepository.existsByPhone(userDto.getPhone())).thenReturn(true);

        verify(userRepository, never()).save(any(User.class));
        verify(webClientService, never()).sendNotification(anyString(), any(EmailStructure.class));
        assertThatThrownBy(() -> userService.create(userDto)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя (ФИО)")
    void checkUpdateShouldUpdateUserFullName() {
        Long id = 1L;
        User user = TestUserData.createTestUser(id);
        UserDto userDto = UserDto.builder()
                .surname("Романенко")
                .name("Роман")
                .patronymic("Романович")
                .build();

        UserSafeDto userSafeDto = TestUserData.createTestUserSafeDto(id);
        userSafeDto.setSurname("Романенко");
        userSafeDto.setName("Роман");
        userSafeDto.setPatronymic("Романович");

        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
        when(userMapper.mapUserToUserSafeDto(user)).thenReturn(userSafeDto);

        assertThat(userService.update(id, userDto)).isEqualTo(userSafeDto);
        verify(webClientService).sendNotification(eq(user.getEmail()), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя (email и телефон)")
    void checkUpdateShouldUpdateUserEmailAndPhoneNumber() {
        Long id = 1L;
        User user = TestUserData.createTestUser(id);
        UserDto userDto = UserDto.builder()
                .email("update123@test.ru")
                .phone("89256543221")
                .build();

        UserSafeDto userSafeDto = TestUserData.createTestUserSafeDto(id);
        userSafeDto.setEmail("update123@test.ru");
        userDto.setPhone("89256543221");

        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
        when(userMapper.mapUserToUserSafeDto(user)).thenReturn(userSafeDto);

        assertThat(userService.update(id, userDto)).isEqualTo(userSafeDto);
        verify(webClientService).sendNotification(eq(userSafeDto.getEmail()), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя при дублировании email")
    void shouldThrowDataIntegrityViolationExceptionIfEmailAlreadyExistsWhenUpdating() {
        Long id1 = 1L;
        Long id2 = 2L;
        User user1 = TestUserData.createTestUser(id1);
        User user2 = TestUserData.createTestUser(id2);
        user2.setEmail("another@mail.ru");

        UserDto userDto = UserDto.builder()
                .email(user1.getEmail())
                .build();

        when(userRepository.findById(id2)).thenReturn(Optional.of(user2));
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.update(id2, userDto)).isInstanceOf(DataIntegrityViolationException.class);
        verify(webClientService, never()).sendNotification(anyString(), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя при дублировании номера телефона")
    void shouldThrowDataIntegrityViolationExceptionIfPhoneNumberAlreadyExistsWhenUpdating() {
        Long id1 = 1L;
        Long id2 = 2L;
        User user1 = TestUserData.createTestUser(id1);
        User user2 = TestUserData.createTestUser(id2);
        user2.setPhone("89256543221");

        UserDto userDto = UserDto.builder()
                .phone(user1.getPhone())
                .build();

        when(userRepository.findById(id2)).thenReturn(Optional.of(user2));
        when(userRepository.existsByPhone(userDto.getPhone())).thenReturn(true);

        assertThatThrownBy(() -> userService.update(id2, userDto)).isInstanceOf(DataIntegrityViolationException.class);
        verify(webClientService, never()).sendNotification(anyString(), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка метода получения пользователя по ID")
    void shouldReturnUserSafeDtoById() {
        Long id = 1L;
        User user = TestUserData.createTestUser(id);
        UserSafeDto userSafeDto = TestUserData.createTestUserSafeDto(id);

        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
        when(userMapper.mapUserToUserSafeDto(user)).thenReturn(userSafeDto);

        UserSafeDto result = userService.get(id);

        assertEquals(result, userSafeDto);
        verify(userRepository).findById(id);
        verify(userMapper).mapUserToUserSafeDto(user);
    }

    @Test
    @DisplayName("Проверка метода получения пользователя по несуществующему ID")
    void shouldThrowObjectNotFoundExceptionIfGetByNonExistentId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(10L)).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Проверка метода удаления пользователя по ID")
    void shouldDeleteUserById() {
        Long id = 1L;
        User user = TestUserData.createTestUser(id);

        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));

        userService.delete(id);

        assertThat(userService.get(id)).isNull();
        verify(userRepository).deleteById(id);
        verify(webClientService).sendNotification(eq(user.getEmail()), any(EmailStructure.class));
    }

    @Test
    @DisplayName("Проверка удаления пользователя по несуществующему ID")
    void shouldThrowObjectNotFoundExceptionIfDeleteByNonExistentId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(anyLong())).isInstanceOf(ObjectNotFoundException.class);
        verify(webClientService, never()).sendNotification(anyString(), any(EmailStructure.class));
    }
}