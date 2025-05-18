package com.fintech.wcm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a role for user authorization.
 * Examples include: ADMIN, OWNER, CFO, ACCOUNTANT, etc.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
    
    // Standard predefined roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_OWNER = "ROLE_OWNER";
    public static final String ROLE_CFO = "ROLE_CFO";
    public static final String ROLE_ACCOUNTANT = "ROLE_ACCOUNTANT";
    public static final String ROLE_USER = "ROLE_USER";
}
