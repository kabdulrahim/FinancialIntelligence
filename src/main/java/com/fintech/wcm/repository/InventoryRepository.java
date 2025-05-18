package com.fintech.wcm.repository;

import com.fintech.wcm.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for accessing and manipulating Inventory entities.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    /**
     * Find inventory items for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of inventory items for the company
     */
    List<Inventory> findByCompanyId(Long companyId);
    
    /**
     * Find inventory items for a specific company with a specific item type.
     * 
     * @param companyId the company ID
     * @param itemType the item type
     * @return a list of inventory items
     */
    List<Inventory> findByCompanyIdAndItemType(Long companyId, Inventory.ItemType itemType);
    
    /**
     * Find inventory items for a specific company with a specific status.
     * 
     * @param companyId the company ID
     * @param status the inventory status
     * @return a list of inventory items
     */
    List<Inventory> findByCompanyIdAndStatus(Long companyId, Inventory.InventoryStatus status);
    
    /**
     * Find inventory items for a specific company with quantity below reorder level.
     * 
     * @param companyId the company ID
     * @return a list of inventory items below reorder level
     */
    @Query("SELECT i FROM Inventory i WHERE i.company.id = :companyId AND i.quantity <= i.reorderLevel")
    List<Inventory> findItemsBelowReorderLevel(@Param("companyId") Long companyId);
    
    /**
     * Calculate the total value of inventory for a company.
     * 
     * @param companyId the company ID
     * @return the total inventory value
     */
    @Query("SELECT SUM(i.totalValue) FROM Inventory i WHERE i.company.id = :companyId AND i.currencyCode = " +
           "(SELECT c.currencyCode FROM Company c WHERE c.id = :companyId)")
    BigDecimal sumTotalInventoryValueByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * Calculate the total inventory value by item type for a company.
     * 
     * @param companyId the company ID
     * @return a list of total values by item type
     */
    @Query("SELECT i.itemType, SUM(i.totalValue) FROM Inventory i WHERE i.company.id = :companyId " +
           "GROUP BY i.itemType ORDER BY SUM(i.totalValue) DESC")
    List<Object[]> sumInventoryValueByItemType(@Param("companyId") Long companyId);
}
