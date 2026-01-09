package ru.klimov.mapper;

import org.mapstruct.Mapper;
import ru.klimov.dto.UserDto;
import ru.klimov.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
}
