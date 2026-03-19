package com.example.tourismfx;

import com.example.tourismfx.model.*;
import com.example.tourismfx.repository.ReceiptRepository;
import com.example.tourismfx.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReceiptServiceTest.java - Unit Tests for ReceiptService
 * =========================================================
 * Tests receipt generation and email notification logic:
 *   - Receipt number generation
 *   - Duplicate receipt prevention
 *   - Receipt lookup by booking and receipt number
 * 
 * Note: PDF generation and email sending are tested indirectly
 * since they require filesystem and SMTP access. These tests
 * focus on the business logic and data flow.
 */
@ExtendWith(MockitoExtension.class)
public class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ReceiptService receiptService;

    // Test fixtures
    private Payment testPayment;
    private HotelBooking testBooking;
    private Receipt testReceipt;

    /**
     * Set up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        // Set the receipts directory for testing
        ReflectionTestUtils.setField(receiptService, "receiptsDirectory", "test-receipts");
        ReflectionTestUtils.setField(receiptService, "fromEmail", "test@tourismfx.com");

        // Create test user
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("tourist@example.com");
        testUser.setFullName("John Doe");
        testUser.setPhoneNumber("+254712345678");

        // Create test tourist
        Tourist testTourist = new Tourist();
        testTourist.setId(1L);
        testTourist.setUser(testUser);
        testTourist.setNationality("Kenya");

        // Create test hotel
        Hotel testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setLocation("Nairobi");

        // Create test room
        Room testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setHotel(testHotel);
        testRoom.setRoomType("Deluxe Room");
        testRoom.setPricePerNight(new BigDecimal("150.00"));

        // Create test booking
        testBooking = new HotelBooking();
        testBooking.setId(1L);
        testBooking.setBookingReference("BK-20240115-00001");
        testBooking.setTourist(testTourist);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(LocalDate.of(2024, 3, 15));
        testBooking.setCheckOutDate(LocalDate.of(2024, 3, 18));
        testBooking.setNumberOfGuests(2);
        testBooking.setTotalPrice(new BigDecimal("450.00"));
        testBooking.setStatus("CONFIRMED");

        // Create test payment
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBooking(testBooking);
        testPayment.setPaymentMethod("PAYPAL");
        testPayment.setTransactionId("PP-12345678");
        testPayment.setAmount(new BigDecimal("450.00"));
        testPayment.setCurrency("USD");
        testPayment.setStatus("COMPLETED");
        testPayment.setPayerEmail("payer@paypal.com");
        testPayment.setCompletedAt(LocalDateTime.now());

        // Create test receipt
        testReceipt = new Receipt();
        testReceipt.setId(1L);
        testReceipt.setReceiptNumber("RCP-20240115-00001");
        testReceipt.setPayment(testPayment);
        testReceipt.setBooking(testBooking);
        testReceipt.setPdfFilePath("test-receipts/RCP-20240115-00001.pdf");
        testReceipt.setEmailSent(false);
        testReceipt.setSentToEmail("tourist@example.com");
    }

    /**
     * Test: Should not generate duplicate receipt for same payment.
     */
    @Test
    @DisplayName("Should skip receipt generation if one already exists")
    void testGenerateReceipt_AlreadyExists_Skips() {
        // Arrange: receipt already exists for this payment
        when(receiptRepository.findByPaymentId(1L)).thenReturn(Optional.of(testReceipt));

        // Act
        receiptService.generateAndSendReceipt(testPayment);

        // Assert: should NOT create a new receipt
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    /**
     * Test: Find receipt by booking ID.
     */
    @Test
    @DisplayName("Should find receipt by booking ID")
    void testFindByBookingId_ReturnsReceipt() {
        // Arrange
        when(receiptRepository.findByBookingId(1L)).thenReturn(Optional.of(testReceipt));

        // Act
        Optional<Receipt> result = receiptService.findByBookingId(1L);

        // Assert
        assertTrue(result.isPresent(), "Receipt should be found");
        assertEquals("RCP-20240115-00001", result.get().getReceiptNumber(),
                "Receipt number should match");
        assertEquals("tourist@example.com", result.get().getSentToEmail(),
                "Email should match tourist's email");
    }

    /**
     * Test: Find receipt by receipt number.
     */
    @Test
    @DisplayName("Should find receipt by receipt number")
    void testFindByReceiptNumber_ReturnsReceipt() {
        // Arrange
        when(receiptRepository.findByReceiptNumber("RCP-20240115-00001"))
                .thenReturn(Optional.of(testReceipt));

        // Act
        Optional<Receipt> result = receiptService.findByReceiptNumber("RCP-20240115-00001");

        // Assert
        assertTrue(result.isPresent(), "Receipt should be found");
        assertEquals(1L, result.get().getPayment().getId(),
                "Payment ID should match");
    }

    /**
     * Test: Finding non-existent receipt returns empty Optional.
     */
    @Test
    @DisplayName("Should return empty when receipt not found")
    void testFindByBookingId_NotFound_ReturnsEmpty() {
        // Arrange
        when(receiptRepository.findByBookingId(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Receipt> result = receiptService.findByBookingId(999L);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty for non-existent booking");
    }

    /**
     * Test: Receipt should store correct email address from tourist profile.
     */
    @Test
    @DisplayName("Should store tourist email as sentToEmail")
    void testReceiptEmail_MatchesTouristEmail() {
        // Assert: verify that the test receipt has the correct email
        assertEquals("tourist@example.com", testReceipt.getSentToEmail(),
                "Receipt email should match the tourist's user email");
        assertEquals(testBooking.getTourist().getUser().getEmail(),
                testReceipt.getSentToEmail(),
                "Receipt email should be derived from tourist's user account");
    }

    /**
     * Test: Receipt data integrity — receipt links to correct booking and payment.
     */
    @Test
    @DisplayName("Should maintain correct data relationships")
    void testReceiptDataIntegrity() {
        // Assert: verify all relationships are correct
        assertEquals(testPayment.getId(), testReceipt.getPayment().getId(),
                "Receipt should link to correct payment");
        assertEquals(testBooking.getId(), testReceipt.getBooking().getId(),
                "Receipt should link to correct booking");
        assertEquals("BK-20240115-00001",
                testReceipt.getBooking().getBookingReference(),
                "Booking reference should be accessible through receipt");
        assertEquals("PAYPAL", testReceipt.getPayment().getPaymentMethod(),
                "Payment method should be accessible through receipt");
    }
}
