package com.fintech.wcm.service.impl;

import com.fintech.wcm.dto.UserDto;
import com.fintech.wcm.exception.BadRequestException;
import com.fintech.wcm.exception.ResourceNotFoundException;
import com.fintech.wcm.model.Company;
import com.fintech.wcm.model.Role;
import com.fintech.wcm.model.User;
import com.fintech.wcm.repository.CompanyRepository;
import com.fintech.wcm.repository.RoleRepository;
import com.fintech.wcm.repository.UserRepository;
import com.fintech.wcm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new BadRequestException("Username already exists: " + userDto.getUsername());
        }
        
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new BadRequestException("Email already exists: " + userDto.getEmail());
        }
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setActive(true);
        
        // Set company if provided
        if (userDto.getCompanyId() != null) {
            Company company = companyRepository.findById(userDto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + userDto.getCompanyId()));
            user.setCompany(company);
        }
        
        // Set roles if provided, otherwise use default USER role
        Set<Role> roles = new HashSet<>();
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            userDto.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            });
        } else {
            Role userRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + Role.ROLE_USER));
            roles.add(userRole);
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Update user information
        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new BadRequestException("Email already exists: " + userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }
        
        if (userDto.isActive() != user.isActive()) {
            user.setActive(userDto.isActive());
        }
        
        // Update company if provided
        if (userDto.getCompanyId() != null && 
                (user.getCompany() == null || !userDto.getCompanyId().equals(user.getCompany().getId()))) {
            Company company = companyRepository.findById(userDto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + userDto.getCompanyId()));
            user.setCompany(company);
        }
        
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToDto(user);
    }

    @Override
    public List<UserDto> getUsersByCompanyId(Long companyId) {
        // Check if company exists
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }
        
        List<User> users = userRepository.findByCompanyId(companyId);
        return users.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDto updateUserRoles(Long id, List<String> roleNames) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        Set<Role> roles = new HashSet<>();
        roleNames.forEach(roleName -> {
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        });
        
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    /**
     * Maps a User entity to a UserDto.
     * 
     * @param user the User entity
     * @return the UserDto
     */
    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setActive(user.isActive());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Set company information if available
        if (user.getCompany() != null) {
            dto.setCompanyId(user.getCompany().getId());
            dto.setCompanyName(user.getCompany().getName());
        }
        
        // Set roles
        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        return dto;
    }
}
