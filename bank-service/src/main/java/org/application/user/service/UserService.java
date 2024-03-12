package org.application.user.service;

import org.application.user.entity.UserDto;
import org.application.user.entity.UserSafeDto;

public interface UserService {
    UserSafeDto create(UserDto userDto);

    UserSafeDto update(Long id, UserDto userDto);

    UserSafeDto get(Long id);

    void delete(Long id);
}
