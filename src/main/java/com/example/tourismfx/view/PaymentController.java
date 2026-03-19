package com.example.tourismfx.view;

import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Payment;
import com.example.tourismfx.service.BookingService;
import com.example.tourismfx.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * PaymentController.java - Payment Processing Controller
 * =========================================================
 * Handles the payment flow for hotel bookings.
 * 
 * Payment flow:
 *   1. Tourist is redirected here after creating a booking
 *   2. Payment page shows booking summary and payment options (PayPal / M-Pesa)
 *   3. Tourist selects a payment method:
 *      - PayPal: Redirected to PayPal approval URL, then back to success handler
 *      - M-Pesa: STK Push sent to phone, tourist waits for confirmation
 *   4. On success: booking confirmed, receipt generated, email sent
 *   5. Tourist redirected to receipt page
 * 
 * URL Mappings:
 *   - GET  /payments/checkout/{bookingId}       : Show payment options
 *   - POST /payments/paypal/initiate            : Start PayPal payment
 *   - GET  /payments/paypal/success             : PayPal return URL (success)
 *   - GET  /payments/paypal/cancel              : PayPal return URL (cancel)
 *   - POST /payments/mpesa/initiate             : Start M-Pesa STK Push
 *   - POST /payments/mpesa/callback             : M-Pesa callback endpoint
 * Template: templates/payment.html
 */
@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    /**
     * Show the payment page with booking summary and payment method selection.
     * 
     * @param bookingId the booking to pay for
     * @param model     the Spring MVC model
     * @return the payment template
     */
    @GetMapping("/checkout/{bookingId}")
    public String showPaymentPage(@PathVariable Long bookingId, Model model) {
        HotelBooking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        // Only show payment page for PENDING bookings
        if (!"PENDING".equals(booking.getStatus())) {
            model.addAttribute("errorMessage",
                    "This booking is already " + booking.getStatus().toLowerCase());
            return "payment";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("hotel", booking.getRoom().getHotel());
        model.addAttribute("room", booking.getRoom());
        return "payment"; // Renders templates/payment.html
    }

    /**
     * Initiate a PayPal payment for a booking.
     * Creates a PayPal order and redirects the tourist to PayPal for approval.
     * 
     * @param bookingId the booking to pay for
     * @param redirectAttributes for flash messages
     * @return redirect to PayPal approval URL
     */
    @PostMapping("/paypal/initiate")
    public String initiatePayPal(@RequestParam Long bookingId,
                                  RedirectAttributes redirectAttributes) {
        try {
            HotelBooking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Create PayPal order and get approval URL
            Map<String, String> result = paymentService.initiatePayPalPayment(booking);

            String approvalUrl = result.get("approvalUrl");
            String paymentId = result.get("paymentId");

            // For mock gateway, the approval URL is a local redirect
            // For real PayPal, this would redirect to https://www.paypal.com/...
            return "redirect:" + approvalUrl + "&paymentId=" + paymentId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "PayPal payment failed: " + e.getMessage());
            return "redirect:/payments/checkout/" + bookingId;
        }
    }

    /**
     * Handle PayPal success callback.
     * Called when the tourist approves the payment on PayPal and is redirected back.
     * Captures the payment and confirms the booking.
     * 
     * @param orderId   the PayPal order ID
     * @param paymentId the local payment record ID
     * @param redirectAttributes for flash messages
     * @return redirect to receipt page on success
     */
    @GetMapping("/paypal/success")
    public String handlePayPalSuccess(@RequestParam String orderId,
                                       @RequestParam Long paymentId,
                                       RedirectAttributes redirectAttributes) {
        try {
            // Capture the PayPal payment (charges the tourist)
            Payment payment = paymentService.completePayPalPayment(paymentId, orderId);

            if ("COMPLETED".equals(payment.getStatus())) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Payment successful! Your booking is confirmed.");
                return "redirect:/receipts/" + payment.getBooking().getId();
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Payment could not be completed. Please try again.");
                return "redirect:/payments/checkout/" + payment.getBooking().getId();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Payment error: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    /**
     * Handle PayPal cancellation.
     * Called when the tourist cancels the payment on PayPal.
     * 
     * @param redirectAttributes for flash messages
     * @return redirect to dashboard
     */
    @GetMapping("/paypal/cancel")
    public String handlePayPalCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "PayPal payment was cancelled. Your booking is still pending.");
        return "redirect:/dashboard";
    }

    /**
     * Initiate an M-Pesa STK Push payment.
     * Sends a payment prompt to the tourist's phone.
     * 
     * @param bookingId   the booking to pay for
     * @param phoneNumber the M-Pesa phone number (format: 254XXXXXXXXX)
     * @param redirectAttributes for flash messages
     * @return redirect to payment status page
     */
    @PostMapping("/mpesa/initiate")
    public String initiateMpesa(@RequestParam Long bookingId,
                                 @RequestParam String phoneNumber,
                                 RedirectAttributes redirectAttributes) {
        try {
            HotelBooking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Initiate M-Pesa STK Push
            Payment payment = paymentService.initiateMpesaPayment(booking, phoneNumber);

            // For mock gateway, simulate immediate success
            // In production, the result comes via M-Pesa callback
            paymentService.handleMpesaCallback(
                    payment.getTransactionId(), "0",
                    "MOCK-RCP-" + System.currentTimeMillis());

            // Reload payment to get updated status
            payment = paymentService.findById(payment.getId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if ("COMPLETED".equals(payment.getStatus())) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "M-Pesa payment successful! Your booking is confirmed.");
                return "redirect:/receipts/" + booking.getId();
            } else {
                redirectAttributes.addFlashAttribute("infoMessage",
                        "M-Pesa payment initiated. Please check your phone and enter PIN.");
                return "redirect:/payments/checkout/" + bookingId;
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "M-Pesa payment failed: " + e.getMessage());
            return "redirect:/payments/checkout/" + bookingId;
        }
    }

    /**
     * M-Pesa callback endpoint (called by Safaricom servers).
     * In production, this receives the payment result from Safaricom.
     * Must be a publicly accessible URL registered with Safaricom.
     * 
     * @param body the callback JSON payload from Safaricom
     * @return "OK" response
     */
    @PostMapping("/mpesa/callback")
    @ResponseBody
    public String handleMpesaCallback(@RequestBody String body) {
        // In production, parse the Safaricom callback JSON and process
        // For now, this endpoint is a placeholder for the Daraja API callback
        return "OK";
    }
}
