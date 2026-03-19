package com.example.tourismfx.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MockPaymentGateway.java - Mock Payment Gateway for Development/Testing
 * =========================================================================
 * Simulates PayPal and M-Pesa payment processing without connecting to
 * real payment APIs. This allows development and testing without:
 *   - PayPal sandbox credentials
 *   - Safaricom Daraja API keys
 *   - Network connectivity to payment providers
 * 
 * All payments are automatically "approved" after a brief simulated delay.
 * Transaction IDs are generated using UUID for uniqueness.
 * 
 * IMPORTANT: Replace this with real payment gateway implementations
 * before deploying to production! See PaymentGateway interface docs.
 * 
 * To switch to real gateways in production:
 *   1. Create RealPaymentGateway implementing PaymentGateway
 *   2. Use @Profile("production") on real, @Profile("dev") on mock
 *   3. Set spring.profiles.active=production in application.properties
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(MockPaymentGateway.class);

    /**
     * Simulate creating a PayPal order.
     * Returns a mock approval URL that points to our own payment success endpoint.
     * In production, this would redirect to PayPal's checkout page.
     */
    @Override
    public Map<String, String> initiatePayPalPayment(BigDecimal amount, String currency,
                                                       String referenceId, String description) {
        logger.info("[MOCK] Creating PayPal order: {} {} for {}", amount, currency, referenceId);

        // Generate a mock PayPal order ID (UUID simulates PayPal's order ID format)
        String mockOrderId = "MOCK-PP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, String> result = new HashMap<>();
        result.put("orderId", mockOrderId);
        // In production, this URL would be PayPal's checkout page
        // For mock, we redirect to our own success handler
        result.put("approvalUrl", "/payments/paypal/success?orderId=" + mockOrderId);
        result.put("status", "CREATED");

        logger.info("[MOCK] PayPal order created: {}", mockOrderId);
        return result;
    }

    /**
     * Simulate capturing a PayPal payment.
     * Always returns success with a mock capture ID and payer email.
     * In production, this would call PayPal's capture API.
     */
    @Override
    public Map<String, String> capturePayPalPayment(String orderId) {
        logger.info("[MOCK] Capturing PayPal order: {}", orderId);

        // Simulate a brief processing delay (100ms)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String mockCaptureId = "MOCK-CAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, String> result = new HashMap<>();
        result.put("status", "COMPLETED");
        result.put("captureId", mockCaptureId);
        result.put("payerEmail", "tourist@example.com"); // Mock payer email

        logger.info("[MOCK] PayPal payment captured: {}", mockCaptureId);
        return result;
    }

    /**
     * Simulate initiating an M-Pesa STK Push.
     * Returns a mock checkout request ID. In production, this would
     * trigger a real STK Push prompt on the tourist's phone.
     * 
     * Note: In production, the actual payment result comes via callback
     * from Safaricom. For mock, we simulate immediate success.
     */
    @Override
    public Map<String, String> initiateMpesaPayment(BigDecimal amount, String phoneNumber,
                                                       String referenceId, String description) {
        logger.info("[MOCK] Initiating M-Pesa STK Push: {} KES to {} for {}",
                amount, phoneNumber, referenceId);

        // Generate mock M-Pesa checkout request ID
        String mockCheckoutId = "MOCK-MPESA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, String> result = new HashMap<>();
        result.put("checkoutRequestId", mockCheckoutId);
        result.put("responseCode", "0");           // 0 = success in M-Pesa
        result.put("responseDescription", "Success. Request accepted for processing");
        result.put("merchantRequestId", "MOCK-MR-" + System.currentTimeMillis());

        logger.info("[MOCK] M-Pesa STK Push initiated: {}", mockCheckoutId);
        return result;
    }
}
