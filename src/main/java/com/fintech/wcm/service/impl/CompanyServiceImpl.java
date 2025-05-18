package com.fintech.wcm.service.impl;

import com.fintech.wcm.dto.CompanyDto;
import com.fintech.wcm.exception.BadRequestException;
import com.fintech.wcm.exception.ResourceNotFoundException;
import com.fintech.wcm.model.Company;
import com.fintech.wcm.model.Currency;
import com.fintech.wcm.repository.CompanyRepository;
import com.fintech.wcm.repository.CurrencyRepository;
import com.fintech.wcm.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CompanyService interface.
 */
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional
    public CompanyDto createCompany(CompanyDto companyDto) {
        // Check if company name or tax ID already exists
        if (companyRepository.existsByName(companyDto.getName())) {
            throw new BadRequestException("Company name already exists: " + companyDto.getName());
        }
        
        if (companyDto.getTaxId() != null && companyRepository.existsByTaxId(companyDto.getTaxId())) {
            throw new BadRequestException("Company tax ID already exists: " + companyDto.getTaxId());
        }
        
        // Validate company type
        Company.CompanyType companyType;
        try {
            companyType = Company.CompanyType.valueOf(companyDto.getType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid company type: " + companyDto.getType() + ". Valid types are SME and MNE");
        }
        
        // Validate currency code
        if (companyDto.getCurrencyCode() == null) {
            throw new BadRequestException("Currency code is required");
        }
        
        Currency currency = currencyRepository.findById(companyDto.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + companyDto.getCurrencyCode()));
        
        // Create and save company
        Company company = new Company();
        company.setName(companyDto.getName());
        company.setType(companyType);
        company.setTaxId(companyDto.getTaxId());
        company.setRegistrationNumber(companyDto.getRegistrationNumber());
        company.setAddress(companyDto.getAddress());
        company.setCity(companyDto.getCity());
        company.setCountry(companyDto.getCountry());
        company.setPostalCode(companyDto.getPostalCode());
        company.setPhone(companyDto.getPhone());
        company.setWebsite(companyDto.getWebsite());
        company.setIndustry(companyDto.getIndustry());
        company.setFiscalYearEnd(companyDto.getFiscalYearEnd());
        company.setCurrencyCode(companyDto.getCurrencyCode());
        company.setActive(true);
        
        Company savedCompany = companyRepository.save(company);
        return mapToDto(savedCompany);
    }

    @Override
    @Transactional
    public CompanyDto updateCompany(Long id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        
        // Check for name uniqueness if name is being changed
        if (companyDto.getName() != null && !companyDto.getName().equals(company.getName()) && 
                companyRepository.existsByName(companyDto.getName())) {
            throw new BadRequestException("Company name already exists: " + companyDto.getName());
        }
        
        // Check for tax ID uniqueness if tax ID is being changed
        if (companyDto.getTaxId() != null && !companyDto.getTaxId().equals(company.getTaxId()) && 
                companyRepository.existsByTaxId(companyDto.getTaxId())) {
            throw new BadRequestException("Company tax ID already exists: " + companyDto.getTaxId());
        }
        
        // Update company type if provided
        if (companyDto.getType() != null) {
            try {
                company.setType(Company.CompanyType.valueOf(companyDto.getType()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid company type: " + companyDto.getType() + ". Valid types are SME and MNE");
            }
        }
        
        // Update currency code if provided
        if (companyDto.getCurrencyCode() != null && !companyDto.getCurrencyCode().equals(company.getCurrencyCode())) {
            currencyRepository.findById(companyDto.getCurrencyCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + companyDto.getCurrencyCode()));
            company.setCurrencyCode(companyDto.getCurrencyCode());
        }
        
        // Update other fields if provided
        if (companyDto.getName() != null) {
            company.setName(companyDto.getName());
        }
        
        if (companyDto.getTaxId() != null) {
            company.setTaxId(companyDto.getTaxId());
        }
        
        if (companyDto.getRegistrationNumber() != null) {
            company.setRegistrationNumber(companyDto.getRegistrationNumber());
        }
        
        if (companyDto.getAddress() != null) {
            company.setAddress(companyDto.getAddress());
        }
        
        if (companyDto.getCity() != null) {
            company.setCity(companyDto.getCity());
        }
        
        if (companyDto.getCountry() != null) {
            company.setCountry(companyDto.getCountry());
        }
        
        if (companyDto.getPostalCode() != null) {
            company.setPostalCode(companyDto.getPostalCode());
        }
        
        if (companyDto.getPhone() != null) {
            company.setPhone(companyDto.getPhone());
        }
        
        if (companyDto.getWebsite() != null) {
            company.setWebsite(companyDto.getWebsite());
        }
        
        if (companyDto.getIndustry() != null) {
            company.setIndustry(companyDto.getIndustry());
        }
        
        if (companyDto.getFiscalYearEnd() != null) {
            company.setFiscalYearEnd(companyDto.getFiscalYearEnd());
        }
        
        Company updatedCompany = companyRepository.save(company);
        return mapToDto(updatedCompany);
    }

    @Override
    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return mapToDto(company);
    }

    @Override
    public CompanyDto getCompanyByName(String name) {
        Company company = companyRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with name: " + name));
        return mapToDto(company);
    }

    @Override
    public List<CompanyDto> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyDto> getCompaniesByType(String type) {
        try {
            Company.CompanyType companyType = Company.CompanyType.valueOf(type);
            List<Company> companies = companyRepository.findByType(companyType);
            return companies.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid company type: " + type + ". Valid types are SME and MNE");
        }
    }

    @Override
    @Transactional
    public void deactivateCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        company.setActive(false);
        companyRepository.save(company);
    }

    @Override
    @Transactional
    public void activateCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        company.setActive(true);
        companyRepository.save(company);
    }

    @Override
    public boolean isCompanyNameAvailable(String name) {
        return !companyRepository.existsByName(name);
    }

    @Override
    public boolean isCompanyTaxIdAvailable(String taxId) {
        return !companyRepository.existsByTaxId(taxId);
    }
    
    /**
     * Maps a Company entity to a CompanyDto.
     * 
     * @param company the Company entity
     * @return the CompanyDto
     */
    private CompanyDto mapToDto(Company company) {
        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .type(company.getType().name())
                .taxId(company.getTaxId())
                .registrationNumber(company.getRegistrationNumber())
                .address(company.getAddress())
                .city(company.getCity())
                .country(company.getCountry())
                .postalCode(company.getPostalCode())
                .phone(company.getPhone())
                .website(company.getWebsite())
                .industry(company.getIndustry())
                .fiscalYearEnd(company.getFiscalYearEnd())
                .currencyCode(company.getCurrencyCode())
                .active(company.isActive())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
