package org.application;

import org.application.user.entity.User;
import org.application.user.entity.UserDto;
import org.application.user.entity.UserSafeDto;

import java.time.LocalDate;

public class TestUserData {
    public static User createTestUser(Long id) {
        return User.builder()
                .id(id)
                .surname("Тестовый")
                .name("Тест")
                .patronymic("Тестович")
                .email("test123@test.ru")
                .birthday(LocalDate.of(1990, 12, 25))
                .phone("89281456781")
                .password("password123test")
                .build();
    }

    public static UserDto createTestUserDto(Long id) {
        return UserDto.builder()
                .id(id)
                .surname("Тестовый")
                .name("Тест")
                .patronymic("Тестович")
                .email("test123@test.ru")
                .birthday(LocalDate.of(1990, 12, 25))
                .phone("89281456781")
                .password("password123test")
                .build();
    }

    public static UserSafeDto createTestUserSafeDto(Long id) {
        return UserSafeDto.builder()
                .id(id)
                .surname("Тестовый")
                .name("Тест")
                .patronymic("Тестович")
                .email("test123@test.ru")
                .birthday(LocalDate.of(1990, 12, 25))
                .phone("89281456781")
                .build();
    }
}
