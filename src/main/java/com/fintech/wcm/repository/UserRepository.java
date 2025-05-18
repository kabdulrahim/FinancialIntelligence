package com.fintech.wcm.repository;

import com.fintech.wcm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by username.
     * 
     * @param username the username
     * @return the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by email.
     * 
     * @param email the email
     * @return the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find users by company ID.
     * 
     * @param companyId the company ID
     * @return list of users in the company
     */
    List<User> findByCompanyId(Long companyId);
    
    /**
     * Check if a username exists.
     * 
     * @param username the username
     * @return true if the username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email exists.
     * 
     * @param email the email
     * @return true if the email exists
     */
    boolean existsByEmail(String email);
}