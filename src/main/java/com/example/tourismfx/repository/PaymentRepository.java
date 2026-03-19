package com.example.tourismfx.repository;

import com.example.tourismfx.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PaymentRepository.java - Data Access Layer for Payment Entity
 * ================================================================
 * Provides database operations for payment transaction records.
 * Supports lookup by booking, transaction ID, and status.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find the payment record for a specific booking.
     * Used to check payment status when viewing booking details.
     * 
     * @param bookingId the booking to find payment for
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByBookingId(Long bookingId);

    /**
     * Find a payment by the external gateway transaction ID.
     * Used for payment verification callbacks from PayPal/M-Pesa.
     * 
     * @param transactionId the external transaction reference
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByTransactionId(String transactionId);
}
