package org.application.user;

import org.application.user.entity.User;
import org.application.user.entity.UserDto;
import org.application.user.entity.UserSafeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User mapUserDtoToUser(UserDto userDto);

    UserSafeDto mapUserToUserSafeDto(User user);
}
