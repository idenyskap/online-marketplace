package com.marketplace.userservice.mapper;

import com.marketplace.userservice.dto.UserDTO;
import com.marketplace.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDTO toDTO(User user);

    List<UserDTO> toDTOList(List<User> users);
}
