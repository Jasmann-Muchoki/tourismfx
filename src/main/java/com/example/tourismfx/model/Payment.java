package com.example.tourismfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment.java - Payment Transaction Entity
 * ============================================
 * Records payment details for a hotel booking.
 * Supports two payment methods:
 *   - PAYPAL: Processed via PayPal Checkout SDK (international tourists)
 *   - MPESA:  Processed via Safaricom Daraja API STK Push (Kenyan tourists)
 * 
 * Payment lifecycle:
 *   1. INITIATED  - Payment request created, awaiting gateway response
 *   2. PROCESSING - Payment is being processed by the gateway
 *   3. COMPLETED  - Payment successful (triggers receipt and email)
 *   4. FAILED     - Payment was declined or errored
 *   5. REFUNDED   - Payment was refunded (booking cancelled)
 * 
 * The transactionId stores the external reference from the payment gateway
 * (e.g., PayPal order ID or M-Pesa receipt number) for reconciliation.
 * 
 * Table: payments
 * Relationships:
 *   - One-to-One with HotelBooking (each payment is for one booking)
 */
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /** Primary key: auto-incremented unique identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Booking: the reservation this payment is for */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private HotelBooking booking;

    /**
     * Payment method: which gateway processed this payment.
     * Values: "PAYPAL" or "MPESA"
     */
    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    /**
     * Transaction ID: external reference from the payment gateway.
     * - PayPal: The PayPal Order ID (e.g., "5O190127TN364715T")
     * - M-Pesa: The M-Pesa receipt number (e.g., "QJI89HGTQ5")
     * Used for payment verification and dispute resolution.
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /** Amount: the exact amount charged to the tourist */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Currency: ISO 4217 currency code (e.g., "USD", "KES") */
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    /**
     * Payment status: current state of the transaction.
     * Values: INITIATED, PROCESSING, COMPLETED, FAILED, REFUNDED
     */
    @Column(nullable = false, length = 20)
    private String status = "INITIATED";

    /** Gateway response: raw response from the payment gateway for debugging */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    /** Payer email: the email associated with the payment account */
    @Column(name = "payer_email", length = 100)
    private String payerEmail;

    /** Phone number: used for M-Pesa STK Push (format: 254XXXXXXXXX) */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /** Timestamp: when the payment was initiated */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp: when the payment was completed or failed */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
