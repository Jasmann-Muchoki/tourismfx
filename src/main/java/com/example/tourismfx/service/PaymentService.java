package com.example.tourismfx.service;

import com.example.tourismfx.gateway.PaymentGateway;
import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Payment;
import com.example.tourismfx.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * PaymentService.java - Payment Processing Service
 * ====================================================
 * Orchestrates payment processing for hotel bookings.
 * 
 * Supports two payment methods:
 *   1. PAYPAL - For international tourists using PayPal accounts/cards
 *   2. MPESA  - For Kenyan tourists using M-Pesa mobile money
 * 
 * Payment flow:
 *   1. Tourist selects payment method on the payment page
 *   2. PaymentService creates a Payment record (status: INITIATED)
 *   3. PaymentGateway initiates the external payment (PayPal/M-Pesa)
 *   4. On success: Payment status → COMPLETED, Booking → CONFIRMED
 *   5. Receipt is generated and email sent (via ReceiptService)
 *   6. On failure: Payment status → FAILED, booking remains PENDING
 * 
 * The PaymentGateway interface abstracts the payment provider,
 * allowing easy switching between real and mock implementations.
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private ReceiptService receiptService;

    /**
     * Initiate a PayPal payment for a booking.
     * Creates a PayPal order and returns the approval URL for redirect.
     * 
     * @param booking the booking to pay for
     * @return map containing "approvalUrl" for redirecting the tourist to PayPal,
     *         and "paymentId" for the local payment record
     */
    @Transactional
    public Map<String, String> initiatePayPalPayment(HotelBooking booking) {
        logger.info("Initiating PayPal payment for booking: {}", booking.getBookingReference());

        // Create a local payment record to track this transaction
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod("PAYPAL");
        payment.setAmount(booking.getTotalPrice());
        payment.setCurrency("USD");
        payment.setStatus("INITIATED");
        payment = paymentRepository.save(payment);

        // Delegate to the PaymentGateway to create the PayPal order
        // Returns a map with approvalUrl (redirect tourist) and orderId
        Map<String, String> result = paymentGateway.initiatePayPalPayment(
                booking.getTotalPrice(),
                "USD",
                booking.getBookingReference(),
                "Hotel booking: " + booking.getBookingReference()
        );

        // Store the PayPal order ID for later verification
        payment.setTransactionId(result.get("orderId"));
        payment.setStatus("PROCESSING");
        paymentRepository.save(payment);

        // Add local payment ID to the result map
        result.put("paymentId", payment.getId().toString());
        return result;
    }

    /**
     * Complete a PayPal payment after tourist approves on PayPal.
     * Called when PayPal redirects back to our success URL.
     * 
     * @param paymentId the local payment record ID
     * @param paypalOrderId the PayPal order ID to capture
     * @return the completed Payment entity
     */
    @Transactional
    public Payment completePayPalPayment(Long paymentId, String paypalOrderId) {
        logger.info("Completing PayPal payment: {} with order: {}", paymentId, paypalOrderId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        // Capture the PayPal order (charge the tourist's account)
        Map<String, String> captureResult = paymentGateway.capturePayPalPayment(paypalOrderId);

        if ("COMPLETED".equals(captureResult.get("status"))) {
            // Payment successful — update payment record
            payment.setStatus("COMPLETED");
            payment.setTransactionId(captureResult.getOrDefault("captureId", paypalOrderId));
            payment.setPayerEmail(captureResult.get("payerEmail"));
            payment.setGatewayResponse(captureResult.toString());
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Confirm the booking (updates status and decrements room availability)
            bookingService.confirmBooking(payment.getBooking().getId());

            // Generate receipt and send confirmation email
            receiptService.generateAndSendReceipt(payment);

            logger.info("PayPal payment completed successfully for booking: {}",
                    payment.getBooking().getBookingReference());
        } else {
            // Payment failed — mark as failed
            payment.setStatus("FAILED");
            payment.setGatewayResponse(captureResult.toString());
            paymentRepository.save(payment);
            logger.error("PayPal payment failed for booking: {}",
                    payment.getBooking().getBookingReference());
        }

        return payment;
    }

    /**
     * Initiate an M-Pesa STK Push payment.
     * Sends a payment prompt to the tourist's phone via Safaricom Daraja API.
     * 
     * @param booking the booking to pay for
     * @param phoneNumber the tourist's M-Pesa phone number (format: 254XXXXXXXXX)
     * @return the created Payment entity
     */
    @Transactional
    public Payment initiateMpesaPayment(HotelBooking booking, String phoneNumber) {
        logger.info("Initiating M-Pesa payment for booking: {} to phone: {}",
                booking.getBookingReference(), phoneNumber);

        // Create a local payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod("MPESA");
        payment.setAmount(booking.getTotalPrice());
        payment.setCurrency("KES");
        payment.setPhoneNumber(phoneNumber);
        payment.setStatus("INITIATED");
        payment = paymentRepository.save(payment);

        // Initiate STK Push via the PaymentGateway
        Map<String, String> result = paymentGateway.initiateMpesaPayment(
                booking.getTotalPrice(),
                phoneNumber,
                booking.getBookingReference(),
                "Hotel booking payment"
        );

        // Update payment with gateway response
        payment.setTransactionId(result.get("checkoutRequestId"));
        payment.setStatus("PROCESSING");
        payment.setGatewayResponse(result.toString());
        paymentRepository.save(payment);

        return payment;
    }

    /**
     * Handle M-Pesa payment callback (called by Safaricom after STK Push).
     * Updates payment status based on the callback result.
     * 
     * @param checkoutRequestId the M-Pesa checkout request ID
     * @param resultCode the M-Pesa result code (0 = success)
     * @param mpesaReceiptNumber the M-Pesa receipt number
     */
    @Transactional
    public void handleMpesaCallback(String checkoutRequestId, String resultCode,
                                     String mpesaReceiptNumber) {
        logger.info("M-Pesa callback received: requestId={}, resultCode={}",
                checkoutRequestId, resultCode);

        Payment payment = paymentRepository.findByTransactionId(checkoutRequestId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for checkout request: " + checkoutRequestId));

        if ("0".equals(resultCode)) {
            // M-Pesa payment successful
            payment.setStatus("COMPLETED");
            payment.setTransactionId(mpesaReceiptNumber); // Update with receipt number
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Confirm booking and generate receipt
            bookingService.confirmBooking(payment.getBooking().getId());
            receiptService.generateAndSendReceipt(payment);

            logger.info("M-Pesa payment completed for booking: {}",
                    payment.getBooking().getBookingReference());
        } else {
            // M-Pesa payment failed
            payment.setStatus("FAILED");
            payment.setGatewayResponse("ResultCode: " + resultCode);
            paymentRepository.save(payment);
            logger.error("M-Pesa payment failed for booking: {}",
                    payment.getBooking().getBookingReference());
        }
    }

    /**
     * Find a payment by its local ID.
     */
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Find the payment for a specific booking.
     */
    public Optional<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
}
