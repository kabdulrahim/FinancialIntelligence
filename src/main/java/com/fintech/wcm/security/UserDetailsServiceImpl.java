package com.fintech.wcm.security;

import com.fintech.wcm.model.User;
import com.fintech.wcm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads user-specific data for authentication.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by username.
     * 
     * @param username the username
     * @return the UserDetails object
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        // Check if user is active
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User is inactive: " + username);
        }
        
        // Get roles and convert to Spring Security's GrantedAuthority
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
        
        // Create UserDetails object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
    
    /**
     * Get the company ID for a user.
     * 
     * @param username the username
     * @return the company ID if available, null otherwise
     */
    @Transactional(readOnly = true)
    public Long getUserCompanyId(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null && user.getCompany() != null) {
            return user.getCompany().getId();
        }
        
        return null;
    }
}
