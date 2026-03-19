package com.example.tourismfx.gateway;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PaymentGateway.java - Payment Gateway Interface
 * ==================================================
 * Defines the contract for payment processing implementations.
 * 
 * This interface abstracts the payment provider, enabling:
 *   1. Easy switching between real and mock implementations
 *   2. Unit testing with mock gateways
 *   3. Adding new payment providers without changing service code
 * 
 * Current implementations:
 *   - MockPaymentGateway: Simulates payments for development/testing
 *   - (Future) RealPaymentGateway: Connects to actual PayPal/M-Pesa APIs
 * 
 * The interface follows the Strategy pattern — PaymentService depends on
 * this interface, and Spring injects the appropriate implementation
 * based on the active profile (dev vs. production).
 */
public interface PaymentGateway {

    /**
     * Initiate a PayPal payment by creating an order.
     * 
     * In production, this calls PayPal's REST API to create an order
     * and returns the approval URL where the tourist completes payment.
     * 
     * @param amount      the payment amount
     * @param currency    ISO 4217 currency code (e.g., "USD")
     * @param referenceId the booking reference for tracking
     * @param description human-readable description of the payment
     * @return map containing:
     *         - "orderId":     PayPal order ID for capture
     *         - "approvalUrl": URL to redirect tourist to PayPal
     *         - "status":      order creation status
     */
    Map<String, String> initiatePayPalPayment(BigDecimal amount, String currency,
                                                String referenceId, String description);

    /**
     * Capture (finalize) a PayPal payment after tourist approval.
     * 
     * Called after the tourist approves payment on PayPal and is redirected
     * back to our application. This step actually charges the tourist.
     * 
     * @param orderId the PayPal order ID to capture
     * @return map containing:
     *         - "status":     "COMPLETED" or "FAILED"
     *         - "captureId":  PayPal capture ID
     *         - "payerEmail": email of the PayPal account used
     */
    Map<String, String> capturePayPalPayment(String orderId);

    /**
     * Initiate an M-Pesa STK Push payment.
     * 
     * Sends a payment prompt to the tourist's phone. The tourist enters
     * their M-Pesa PIN on their phone to authorize the payment.
     * The result is delivered via callback (handleMpesaCallback).
     * 
     * @param amount      the payment amount in KES
     * @param phoneNumber M-Pesa phone number (format: 254XXXXXXXXX)
     * @param referenceId the booking reference for tracking
     * @param description human-readable description
     * @return map containing:
     *         - "checkoutRequestId": M-Pesa request ID for tracking
     *         - "responseCode":      "0" for success
     *         - "responseDescription": human-readable status
     */
    Map<String, String> initiateMpesaPayment(BigDecimal amount, String phoneNumber,
                                               String referenceId, String description);
}
