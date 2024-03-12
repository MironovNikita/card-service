package org.application.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.common.validation.Create;
import org.application.common.validation.Update;
import org.application.user.entity.UserDto;
import org.application.user.entity.UserSafeDto;
import org.application.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSafeDto create(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("UserController: Запрос на создание пользователя {}", userDto.getName());
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserSafeDto update(@PathVariable Long id, @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("UserController: Запрос на обновление пользователя по ID {}", id);
        return userService.update(id, userDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserSafeDto get(@PathVariable Long id) {
        log.info("UserController: Запрос на получение пользователя по ID {}", id);
        return userService.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        log.info("UserController: Запрос на удаление пользователя по ID {}", id);
        userService.delete(id);
    }
}
