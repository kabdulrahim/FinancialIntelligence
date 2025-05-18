package com.fintech.wcm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for data import results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResultDto {
    
    private String importType;
    private String source;
    private String fileName;
    private LocalDateTime importDate;
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private String status;  // COMPLETED, PARTIALLY_COMPLETED, FAILED
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private String summary;
    
    /**
     * Add an error message to the list of errors.
     * 
     * @param error the error message
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
    
    /**
     * Add a warning message to the list of warnings.
     * 
     * @param warning the warning message
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }
}
