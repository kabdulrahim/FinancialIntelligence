package com.fintech.wcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Working Capital Management system.
 * This enterprise-grade application is designed to help businesses
 * of all sizes manage their working capital effectively.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class WorkingCapitalManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkingCapitalManagementApplication.class, args);
    }
}
