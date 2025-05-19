package com.fintech.wcm.repository;

import com.fintech.wcm.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find a role by its name.
     * 
     * @param name the role name
     * @return the role if found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if a role exists by name.
     * 
     * @param name the role name
     * @return true if exists
     */
    boolean existsByName(String name);
}