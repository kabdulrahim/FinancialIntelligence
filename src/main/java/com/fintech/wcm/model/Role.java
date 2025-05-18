package com.fintech.wcm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Entity class for roles in the application.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_OWNER = "ROLE_OWNER";
    public static final String ROLE_CFO = "ROLE_CFO";
    public static final String ROLE_ACCOUNTANT = "ROLE_ACCOUNTANT";
    public static final String ROLE_USER = "ROLE_USER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}