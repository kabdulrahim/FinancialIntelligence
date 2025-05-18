package com.fintech.wcm.service;

import com.fintech.wcm.dto.UserDto;

import java.util.List;

/**
 * Service interface for user management.
 */
public interface UserService {
    
    /**
     * Create a new user.
     * 
     * @param userDto the user data
     * @return the created user
     */
    UserDto createUser(UserDto userDto);
    
    /**
     * Update an existing user.
     * 
     * @param id the user ID
     * @param userDto the updated user data
     * @return the updated user
     */
    UserDto updateUser(Long id, UserDto userDto);
    
    /**
     * Get a user by ID.
     * 
     * @param id the user ID
     * @return the user
     */
    UserDto getUserById(Long id);
    
    /**
     * Get a user by username.
     * 
     * @param username the username
     * @return the user
     */
    UserDto getUserByUsername(String username);
    
    /**
     * Get users by company ID.
     * 
     * @param companyId the company ID
     * @return list of users in the company
     */
    List<UserDto> getUsersByCompanyId(Long companyId);
    
    /**
     * Delete a user.
     * 
     * @param id the user ID
     */
    void deleteUser(Long id);
    
    /**
     * Change a user's password.
     * 
     * @param id the user ID
     * @param currentPassword the current password
     * @param newPassword the new password
     */
    void changePassword(Long id, String currentPassword, String newPassword);
    
    /**
     * Update a user's roles.
     * 
     * @param id the user ID
     * @param roleNames the role names
     * @return the updated user
     */
    UserDto updateUserRoles(Long id, List<String> roleNames);
    
    /**
     * Check if a username is available.
     * 
     * @param username the username
     * @return true if available
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Check if an email is available.
     * 
     * @param email the email
     * @return true if available
     */
    boolean isEmailAvailable(String email);
}