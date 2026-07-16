package com.project.public_safety_app.service;

import com.project.public_safety_app.dto.EmergencyContactDto;
import com.project.public_safety_app.dto.UserDto;
import com.project.public_safety_app.dto.UserResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userdto);
    UserDto login(String userName, String password);
    UserDto updateUser(Long userId, UserDto userDto);

    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id")
    void deleteUser(Long userId);

    UserDto getUserByName(String name);

    @PreAuthorize("hasRole('ROLE_ADMIN') or #name == authentication.principal.username")
    void deleteUserByName(String name);

    List<EmergencyContactDto> getUserContactsByUsername(String username);
    List<UserResponse> getAllUsers();
    UserResponse getUser(String userName);
}