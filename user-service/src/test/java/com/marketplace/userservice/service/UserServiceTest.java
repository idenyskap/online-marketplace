package com.marketplace.userservice.service;

import com.marketplace.userservice.dto.UserDTO;
import com.marketplace.userservice.entity.User;
import com.marketplace.userservice.enums.Role;
import com.marketplace.userservice.exception.ResourceNotFoundException;
import com.marketplace.userservice.mapper.UserMapper;
import com.marketplace.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Dou")
                .role(Role.BUYER)
                .build();

        testUserDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Dou")
                .role("BUYER")
                .build();
    }

    @Test
    void shouldReturnUserWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Dou", result.getLastName());
        assertEquals("BUYER", result.getRole());
    }

    @Test
    void shouldReturnAllUsersWhenFound() {
        List<User> users = List.of(testUser);
        List<UserDTO> userDTOs = List.of(testUserDTO);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDTOList(users)).thenReturn(userDTOs);

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDTO.getId(), result.get(0).getId());

        verify(userRepository).findAll();
        verify(userMapper).toDTOList(users);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(1L));
    }
}
