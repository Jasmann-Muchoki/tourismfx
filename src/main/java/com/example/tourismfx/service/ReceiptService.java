package com.example.tourismfx.service;

import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Payment;
import com.example.tourismfx.model.Receipt;
import com.example.tourismfx.repository.ReceiptRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * ReceiptService.java - Receipt Generation and Email Service
 * =============================================================
 * Handles PDF receipt generation and email notifications for
 * successfully completed bookings.
 * 
 * Receipt generation flow:
 *   1. Payment completes successfully (PayPal or M-Pesa)
 *   2. PaymentService calls generateAndSendReceipt()
 *   3. This service generates a unique receipt number
 *   4. Builds an HTML receipt from booking/payment data
 *   5. Converts HTML to PDF using iText html2pdf
 *   6. Saves PDF to filesystem and receipt record to database
 *   7. Sends confirmation email with PDF attachment (@Async)
 * 
 * The @Async annotation on sendReceiptEmail() ensures email sending
 * runs in a background thread, so the tourist isn't kept waiting.
 */
@Service
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private JavaMailSender mailSender;

    /** Directory where PDF receipts are stored on the server */
    @Value("${app.receipts.directory:receipts}")
    private String receiptsDirectory;

    /** Sender email address for receipt notifications */
    @Value("${spring.mail.username:noreply@tourismfx.com}")
    private String fromEmail;

    /**
     * Generate a PDF receipt and send it via email.
     * This is the main entry point called by PaymentService after successful payment.
     * 
     * @param payment the completed payment to generate a receipt for
     */
    @Transactional
    public void generateAndSendReceipt(Payment payment) {
        logger.info("Generating receipt for payment: {}", payment.getId());

        // Check if receipt already exists for this payment
        if (receiptRepository.findByPaymentId(payment.getId()).isPresent()) {
            logger.warn("Receipt already exists for payment: {}", payment.getId());
            return;
        }

        // Generate unique receipt number: RCP-YYYYMMDD-XXXXX
        String receiptNumber = generateReceiptNumber();

        // Build HTML content for the receipt
        String htmlContent = buildReceiptHtml(payment, receiptNumber);

        // Generate PDF from HTML
        String pdfPath = generatePdf(htmlContent, receiptNumber);

        // Create and save the receipt record
        Receipt receipt = new Receipt();
        receipt.setReceiptNumber(receiptNumber);
        receipt.setPayment(payment);
        receipt.setBooking(payment.getBooking());
        receipt.setPdfFilePath(pdfPath);
        receipt.setSentToEmail(payment.getBooking().getTourist().getUser().getEmail());
        receipt = receiptRepository.save(receipt);

        // Send email in background thread (non-blocking)
        sendReceiptEmail(receipt, pdfPath);
    }

    /**
     * Build HTML content for the receipt PDF.
     * Creates a professionally formatted receipt with booking and payment details.
     * 
     * @param payment the payment record
     * @param receiptNumber the generated receipt number
     * @return HTML string ready for PDF conversion
     */
    private String buildReceiptHtml(Payment payment, String receiptNumber) {
        HotelBooking booking = payment.getBooking();
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());

        // Build a clean, printable HTML receipt
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");

        // Receipt CSS: clean, professional print-friendly styling
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; color: #333; }");
        html.append(".header { text-align: center; border-bottom: 3px solid #1a73e8; padding-bottom: 15px; }");
        html.append(".header h1 { color: #1a73e8; margin: 0; font-size: 28px; }");
        html.append(".header p { color: #666; margin: 5px 0; }");
        html.append(".receipt-info { background: #f8f9fa; padding: 15px; margin: 20px 0; border-radius: 8px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("th { background: #1a73e8; color: white; }");
        html.append(".total { font-size: 20px; font-weight: bold; color: #1a73e8; text-align: right; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }");
        html.append(".status { display: inline-block; padding: 5px 15px; background: #28a745; color: white; border-radius: 20px; }");

        html.append("</style></head><body>");

        // Header section
        html.append("<div class='header'>");
        html.append("<h1>🌍 TourismFX</h1>");
        html.append("<p>Tourism Docket Management System</p>");
        html.append("<p>Booking Receipt</p>");
        html.append("</div>");

        // Receipt info box
        html.append("<div class='receipt-info'>");
        html.append("<strong>Receipt No:</strong> ").append(receiptNumber).append("<br/>");
        html.append("<strong>Date:</strong> ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm"))).append("<br/>");
        html.append("<strong>Status:</strong> <span class='status'>PAID</span>");
        html.append("</div>");

        // Guest details table
        html.append("<h3>Guest Details</h3>");
        html.append("<table>");
        html.append("<tr><td><strong>Name</strong></td><td>")
                .append(booking.getTourist().getUser().getFullName()).append("</td></tr>");
        html.append("<tr><td><strong>Email</strong></td><td>")
                .append(booking.getTourist().getUser().getEmail()).append("</td></tr>");
        html.append("<tr><td><strong>Nationality</strong></td><td>")
                .append(booking.getTourist().getNationality()).append("</td></tr>");
        html.append("</table>");

        // Booking details table
        html.append("<h3>Booking Details</h3>");
        html.append("<table>");
        html.append("<tr><th>Item</th><th>Details</th></tr>");
        html.append("<tr><td>Booking Ref</td><td>").append(booking.getBookingReference()).append("</td></tr>");
        html.append("<tr><td>Hotel</td><td>").append(booking.getRoom().getHotel().getName()).append("</td></tr>");
        html.append("<tr><td>Room Type</td><td>").append(booking.getRoom().getRoomType()).append("</td></tr>");
        html.append("<tr><td>Check-in</td><td>").append(booking.getCheckInDate()).append("</td></tr>");
        html.append("<tr><td>Check-out</td><td>").append(booking.getCheckOutDate()).append("</td></tr>");
        html.append("<tr><td>Nights</td><td>").append(nights).append("</td></tr>");
        html.append("<tr><td>Guests</td><td>").append(booking.getNumberOfGuests()).append("</td></tr>");
        html.append("</table>");

        // Payment details table
        html.append("<h3>Payment Details</h3>");
        html.append("<table>");
        html.append("<tr><th>Item</th><th>Details</th></tr>");
        html.append("<tr><td>Payment Method</td><td>").append(payment.getPaymentMethod()).append("</td></tr>");
        html.append("<tr><td>Transaction ID</td><td>").append(payment.getTransactionId()).append("</td></tr>");
        html.append("<tr><td>Currency</td><td>").append(payment.getCurrency()).append("</td></tr>");
        html.append("</table>");

        // Total amount
        html.append("<div class='total'>Total: ").append(payment.getCurrency())
                .append(" ").append(payment.getAmount()).append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Thank you for choosing TourismFX! We wish you a wonderful stay.</p>");
        html.append("<p>For support, contact: support@tourismfx.com | +254 700 000 000</p>");
        html.append("<p>This is a computer-generated receipt and does not require a signature.</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Generate a PDF file from HTML content using iText html2pdf.
     * 
     * @param htmlContent the HTML receipt content
     * @param receiptNumber the receipt number (used as filename)
     * @return the file path of the generated PDF
     */
    private String generatePdf(String htmlContent, String receiptNumber) {
        // Ensure the receipts directory exists
        File directory = new File(receiptsDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = receiptsDirectory + File.separator + receiptNumber + ".pdf";

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // Convert HTML to PDF using iText's HtmlConverter
            HtmlConverter.convertToPdf(htmlContent, fos);
            logger.info("PDF receipt generated: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to generate PDF receipt: {}", e.getMessage());
            throw new RuntimeException("Failed to generate receipt PDF", e);
        }

        return filePath;
    }

    /**
     * Send the receipt via email with PDF attachment.
     * Runs asynchronously (@Async) to avoid blocking the payment flow.
     * 
     * @param receipt the receipt record
     * @param pdfPath path to the PDF file to attach
     */
    @Async
    public void sendReceiptEmail(Receipt receipt, String pdfPath) {
        try {
            String toEmail = receipt.getSentToEmail();
            String bookingRef = receipt.getBooking().getBookingReference();

            // Create a MIME message with HTML body and PDF attachment
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TourismFX - Booking Confirmation: " + bookingRef);

            // HTML email body with booking summary
            String emailBody = buildConfirmationEmailHtml(receipt);
            helper.setText(emailBody, true); // true = HTML content

            // Attach the PDF receipt
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                helper.addAttachment("Receipt-" + receipt.getReceiptNumber() + ".pdf", pdfFile);
            }

            // Send the email
            mailSender.send(message);

            // Update receipt record to mark email as sent
            receipt.setEmailSent(true);
            receiptRepository.save(receipt);

            logger.info("Receipt email sent to: {} for booking: {}", toEmail, bookingRef);

        } catch (MessagingException e) {
            logger.error("Failed to send receipt email: {}", e.getMessage());
            // Don't throw — email failure shouldn't break the booking flow
        }
    }

    /**
     * Build HTML content for the confirmation email.
     * 
     * @param receipt the receipt with booking details
     * @return HTML string for the email body
     */
    private String buildConfirmationEmailHtml(Receipt receipt) {
        HotelBooking booking = receipt.getBooking();
        StringBuilder html = new StringBuilder();

        html.append("<html><body style='font-family: Segoe UI, Arial, sans-serif; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");

        // Email header
        html.append("<div style='text-align: center; background: linear-gradient(135deg, #1a73e8, #00c6ff); ");
        html.append("padding: 30px; border-radius: 10px 10px 0 0;'>");
        html.append("<h1 style='color: white; margin: 0;'>Booking Confirmed!</h1>");
        html.append("<p style='color: #e0e0e0;'>Your reservation has been successfully processed</p>");
        html.append("</div>");

        // Booking summary
        html.append("<div style='background: #f8f9fa; padding: 20px; border: 1px solid #ddd;'>");
        html.append("<h3 style='color: #1a73e8;'>Booking Summary</h3>");
        html.append("<p><strong>Booking Reference:</strong> ").append(booking.getBookingReference()).append("</p>");
        html.append("<p><strong>Hotel:</strong> ").append(booking.getRoom().getHotel().getName()).append("</p>");
        html.append("<p><strong>Room:</strong> ").append(booking.getRoom().getRoomType()).append("</p>");
        html.append("<p><strong>Check-in:</strong> ").append(booking.getCheckInDate()).append("</p>");
        html.append("<p><strong>Check-out:</strong> ").append(booking.getCheckOutDate()).append("</p>");
        html.append("<p><strong>Amount Paid:</strong> ").append(receipt.getPayment().getCurrency())
                .append(" ").append(receipt.getPayment().getAmount()).append("</p>");
        html.append("<p><strong>Receipt No:</strong> ").append(receipt.getReceiptNumber()).append("</p>");
        html.append("</div>");

        // Note about attached PDF
        html.append("<div style='padding: 15px; text-align: center;'>");
        html.append("<p>📎 Your detailed receipt is attached to this email as a PDF.</p>");
        html.append("<p>You can also print your receipt from your dashboard.</p>");
        html.append("</div>");

        // Footer
        html.append("<div style='text-align: center; padding: 15px; color: #999; font-size: 12px;'>");
        html.append("<p>TourismFX - Tourism Docket Management System</p>");
        html.append("<p>support@tourismfx.com | +254 700 000 000</p>");
        html.append("</div>");

        html.append("</div></body></html>");
        return html.toString();
    }

    /**
     * Generate a unique receipt number.
     * Format: RCP-YYYYMMDD-XXXXX
     */
    private String generateReceiptNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = receiptRepository.count() + 1;
        return String.format("RCP-%s-%05d", datePart, count);
    }

    /**
     * Find a receipt by booking ID (for dashboard display).
     */
    public Optional<Receipt> findByBookingId(Long bookingId) {
        return receiptRepository.findByBookingId(bookingId);
    }

    /**
     * Find a receipt by receipt number (for PDF download).
     */
    public Optional<Receipt> findByReceiptNumber(String receiptNumber) {
        return receiptRepository.findByReceiptNumber(receiptNumber);
    }
}
