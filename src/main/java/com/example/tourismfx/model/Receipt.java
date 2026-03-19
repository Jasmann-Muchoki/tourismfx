package com.example.tourismfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Receipt.java - Booking Receipt Entity
 * ========================================
 * Stores generated receipt records for completed bookings.
 * 
 * When a payment is successfully completed:
 *   1. A Receipt record is created in the database
 *   2. A PDF file is generated and stored (path saved in pdfFilePath)
 *   3. The PDF is attached to a confirmation email sent to the tourist
 *   4. The tourist can also view/print the receipt from their dashboard
 * 
 * The receipt contains all booking details, payment information,
 * and a unique receipt number for record-keeping.
 * 
 * Table: receipts
 * Relationships:
 *   - One-to-One with Payment (each receipt corresponds to one payment)
 *   - Many-to-One with HotelBooking (linked through the payment)
 */
@Entity
@Table(name = "receipts")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    /** Primary key: auto-incremented unique identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Receipt number: unique human-readable identifier.
     * Format: "RCP-YYYYMMDD-XXXXX" (e.g., "RCP-20240115-00042")
     * Printed on the PDF receipt and referenced in emails.
     */
    @Column(name = "receipt_number", unique = true, nullable = false, length = 30)
    private String receiptNumber;

    /** Payment: the completed payment this receipt documents */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    /** Booking: direct reference to the booking for convenience */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private HotelBooking booking;

    /**
     * PDF file path: location of the generated PDF receipt on the server.
     * The PDF is served via the ReceiptController for download/print.
     */
    @Column(name = "pdf_file_path", length = 500)
    private String pdfFilePath;

    /** Flag indicating whether the receipt email was sent successfully */
    @Column(name = "email_sent")
    private boolean emailSent = false;

    /** Email address the receipt was sent to */
    @Column(name = "sent_to_email", length = 100)
    private String sentToEmail;

    /** Timestamp: when the receipt was generated */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
