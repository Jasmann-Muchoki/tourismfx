package com.example.tourismfx.repository;

import com.example.tourismfx.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ReceiptRepository.java - Data Access Layer for Receipt Entity
 * ================================================================
 * Provides database operations for booking receipt records.
 * Receipts are generated after successful payment completion.
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    /**
     * Find a receipt by its unique receipt number.
     * Used for receipt lookup and PDF download.
     * 
     * @param receiptNumber the receipt number (e.g., "RCP-20240115-00042")
     * @return Optional containing the receipt if found
     */
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    /**
     * Find a receipt by the payment ID it documents.
     * Used after payment completion to check if a receipt already exists.
     * 
     * @param paymentId the payment ID
     * @return Optional containing the receipt if found
     */
    Optional<Receipt> findByPaymentId(Long paymentId);

    /**
     * Find a receipt by the booking ID.
     * Used on the tourist dashboard to provide receipt download links.
     * 
     * @param bookingId the booking ID
     * @return Optional containing the receipt if found
     */
    Optional<Receipt> findByBookingId(Long bookingId);
}
