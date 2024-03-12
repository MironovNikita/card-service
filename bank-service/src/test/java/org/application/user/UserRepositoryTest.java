package org.application.user;

import org.application.TestUserData;
import org.application.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void deleteUsers() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Проверка поиска сохранённого в БД пользователя по email")
    void shouldFindExistingUserByEmail() {
        User user = TestUserData.createTestUser(1L);
        String email = user.getEmail();

        userRepository.save(user);

        assertTrue(userRepository.existsByEmail(email));
    }

    @Test
    @DisplayName("Проверка поиска несохранённого в БД пользователя по email")
    void shouldNotFindExistingUserByEmail() {
        User user = TestUserData.createTestUser(1L);
        String email = user.getEmail();

        assertFalse(userRepository.existsByEmail(email));
    }

    @Test
    @DisplayName("Проверка поиска сохранённого в БД пользователя по номеру телефона")
    void shouldFindExistingUserByPhoneNumber() {
        User user = TestUserData.createTestUser(1L);
        String phone = user.getPhone();

        userRepository.save(user);

        assertTrue(userRepository.existsByPhone(phone));
    }

    @Test
    @DisplayName("Проверка поиска несохранённого в БД пользователя по номеру телефона")
    void shouldNotFindExistingUserByPhoneNumber() {
        User user = TestUserData.createTestUser(1L);
        String phone = user.getPhone();

        assertFalse(userRepository.existsByPhone(phone));
    }
}