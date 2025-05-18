package com.fintech.wcm.controller;

import com.fintech.wcm.dto.UserDto;
import com.fintech.wcm.exception.BadRequestException;
import com.fintech.wcm.model.User;
import com.fintech.wcm.security.JwtTokenProvider;
import com.fintech.wcm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    /**
     * Endpoint for user login.
     * 
     * @param loginRequest the login request containing username and password
     * @return the JWT token and user details
     */
    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            throw new BadRequestException("Username and password are required");
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(userDetails);
        
        // Get user details
        UserDto userDto = userService.getUserByUsername(username);
        
        // Update last login time - in a real app, this should be done in the service layer
        // userService.updateLastLoginTime(userDto.getId(), LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userDto);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for user registration.
     * 
     * @param userDto the user data
     * @return the created user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user")
    public ResponseEntity<UserDto> register(@RequestBody UserDto userDto) {
        if (userDto.getUsername() == null || userDto.getEmail() == null || userDto.getPassword() == null) {
            throw new BadRequestException("Username, email, and password are required");
        }
        
        UserDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Endpoint to check if a username is available.
     * 
     * @param request the request containing the username
     * @return a response indicating if the username is available
     */
    @PostMapping("/check-username")
    @Operation(summary = "Check if a username is available", description = "Checks if a username is available for registration")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        
        if (username == null) {
            throw new BadRequestException("Username is required");
        }
        
        boolean available = userService.isUsernameAvailable(username);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to check if an email is available.
     * 
     * @param request the request containing the email
     * @return a response indicating if the email is available
     */
    @PostMapping("/check-email")
    @Operation(summary = "Check if an email is available", description = "Checks if an email is available for registration")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null) {
            throw new BadRequestException("Email is required");
        }
        
        boolean available = userService.isEmailAvailable(email);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }
}
