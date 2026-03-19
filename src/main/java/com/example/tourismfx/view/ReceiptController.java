package com.example.tourismfx.view;

import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Payment;
import com.example.tourismfx.model.Receipt;
import com.example.tourismfx.service.BookingService;
import com.example.tourismfx.service.PaymentService;
import com.example.tourismfx.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.time.temporal.ChronoUnit;

/**
 * ReceiptController.java - Receipt Viewing and Download Controller
 * ===================================================================
 * Handles receipt display and PDF download for confirmed bookings.
 * 
 * Features:
 *   - View receipt details in HTML format (printable page)
 *   - Download receipt as PDF file
 *   - Print-friendly receipt template with @media print CSS
 * 
 * Tourists can:
 *   1. View their receipt online (GET /receipts/{bookingId})
 *   2. Print the receipt page (browser print dialog with optimized CSS)
 *   3. Download the PDF receipt (GET /receipts/{bookingId}/download)
 * 
 * URL Mappings:
 *   - GET /receipts/{bookingId}          : View receipt HTML page
 *   - GET /receipts/{bookingId}/download : Download receipt PDF
 * Template: templates/receipt.html
 */
@Controller
@RequestMapping("/receipts")
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    /**
     * Display the receipt page in HTML format.
     * This page is print-friendly — the template includes @media print
     * CSS that hides navigation and optimizes layout for printing.
     * 
     * @param bookingId the booking ID to show receipt for
     * @param model     the Spring MVC model
     * @return the receipt template name
     */
    @GetMapping("/{bookingId}")
    public String viewReceipt(@PathVariable Long bookingId, Model model) {
        // Load booking details
        HotelBooking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        // Load payment details
        Payment payment = paymentService.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found for booking: " + bookingId));

        // Load receipt record
        Receipt receipt = receiptService.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Receipt not found for booking: " + bookingId));

        // Calculate number of nights for display
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());

        // Pass all data to the template
        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("receipt", receipt);
        model.addAttribute("tourist", booking.getTourist());
        model.addAttribute("hotel", booking.getRoom().getHotel());
        model.addAttribute("room", booking.getRoom());
        model.addAttribute("nights", nights);

        return "receipt"; // Renders templates/receipt.html
    }

    /**
     * Download the receipt as a PDF file.
     * Serves the pre-generated PDF file from the filesystem.
     * 
     * The response includes proper Content-Disposition header to trigger
     * a file download dialog in the browser.
     * 
     * @param bookingId the booking ID
     * @return ResponseEntity with the PDF file as the response body
     */
    @GetMapping("/{bookingId}/download")
    public ResponseEntity<Resource> downloadReceipt(@PathVariable Long bookingId) {
        // Find the receipt record to get the PDF file path
        Receipt receipt = receiptService.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Receipt not found for booking: " + bookingId));

        // Load the PDF file from the filesystem
        File pdfFile = new File(receipt.getPdfFilePath());
        if (!pdfFile.exists()) {
            throw new RuntimeException("Receipt PDF file not found on server");
        }

        Resource resource = new FileSystemResource(pdfFile);

        // Return the PDF with download headers
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Receipt-" + receipt.getReceiptNumber() + ".pdf\"")
                .body(resource);
    }
}
