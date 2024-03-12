package org.application.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.client.WebClientService;
import org.application.common.constants.EmailMessages;
import org.application.common.entity.EmailStructure;
import org.application.common.exception.ObjectNotFoundException;
import org.application.common.security.PasswordHandler;
import org.application.user.UserMapper;
import org.application.user.UserRepository;
import org.application.user.entity.User;
import org.application.user.entity.UserDto;
import org.application.user.entity.UserSafeDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.lang.String.format;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordHandler passwordHandler;
    private final WebClientService webClientService;

    @Override
    @Transactional
    public UserSafeDto create(UserDto userDto) {
        checkIfUserAlreadyExistsByEmail(userDto.getEmail());
        checkIfUserAlreadyExistsByPhone(userDto.getPhone());

        userDto.setPassword(passwordHandler.encodePassword(userDto.getPassword()));
        User user = userMapper.mapUserDtoToUser(userDto);

        sendNotification(user.getEmail(), EmailMessages.WELCOME_USER_SUBJECT,
                EmailMessages.greetUser(user.getName(), user.getPatronymic()));

        return userMapper.mapUserToUserSafeDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserSafeDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь с ID {} не найден!", id);
            return new ObjectNotFoundException("Пользователь", id);
        });

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            checkIfUserAlreadyExistsByEmail(userDto.getEmail());
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getPhone() != null && !userDto.getPhone().equals(user.getPhone())) {
            checkIfUserAlreadyExistsByPhone(userDto.getPhone());
            user.setPhone(userDto.getPhone());
        }

        if (userDto.getPassword() != null) {
            user.setPassword(passwordHandler.encodePassword(userDto.getPassword()));
        }

        Optional.ofNullable(userDto.getSurname()).ifPresent(user::setSurname);
        Optional.ofNullable(userDto.getName()).ifPresent(user::setName);
        Optional.ofNullable(userDto.getPatronymic()).ifPresent(user::setPatronymic);
        Optional.ofNullable(userDto.getBirthday()).ifPresent(user::setBirthday);

        sendNotification(user.getEmail(), EmailMessages.UPDATE_USER_SUBJECT,
                EmailMessages.updateUser(user.getName(), user.getPatronymic()));

        return userMapper.mapUserToUserSafeDto(user);
    }

    @Override
    public UserSafeDto get(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь с таким ID {} не найден!", id);
            return new ObjectNotFoundException("Пользователь", id);
        });

        return userMapper.mapUserToUserSafeDto(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь с таким ID {} не найден!", id);
            return new ObjectNotFoundException("Пользователь", id);
        });

        sendNotification(user.getEmail(), EmailMessages.DELETE_USER_SUBJECT,
                EmailMessages.deleteUser(user.getName(), user.getPatronymic()));

        userRepository.deleteById(id);
    }

    private void checkIfUserAlreadyExistsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Пользователь с таким email {} уже существует", email);
            throw new DataIntegrityViolationException(format("Пользователь с таким email (%s) уже существует", email));
        }
    }

    private void checkIfUserAlreadyExistsByPhone(String phone) {
        if (userRepository.existsByPhone(phone)) {
            log.error("Пользователь с таким номером телефона {} уже существует", phone);
            throw new DataIntegrityViolationException(
                    format("Пользователь с таким номером телефона (%s) уже существует", phone));
        }
    }

    private void sendNotification(String email, String subject, String message) {
        EmailStructure emailStructure = new EmailStructure(subject, message);

        webClientService.sendNotification(email, emailStructure);
    }
}
