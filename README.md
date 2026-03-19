# TourismFX - Tourism Docket Management System

A professional Spring Boot web application for managing tourism bookings with integrated PayPal and M-Pesa payment processing, PDF receipt generation, and email notifications.

![Java](https://img.shields.io/badge/Java-17+-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green) ![MySQL](https://img.shields.io/badge/MySQL-8.x-orange) ![License](https://img.shields.io/badge/License-MIT-yellow)

## Features

- **Hotel Management** — Browse, search, and filter hotels by name, location, and star rating
- **Booking System** — Create, confirm, and cancel hotel reservations with date validation and price calculation
- **Payment Integration** — PayPal and M-Pesa (Safaricom Daraja API) payment processing with mock gateway for development
- **PDF Receipts** — Automatic PDF receipt generation using iText html2pdf
- **Email Notifications** — Confirmation emails with PDF attachments sent via Spring Mail
- **User Authentication** — Spring Security with BCrypt password hashing, role-based access (ROLE_TOURIST, ROLE_ADMIN)
- **Responsive UI** — Futuristic dark theme with Bootstrap 5, glassmorphism effects, and mobile-first design
- **Print Support** — Print-friendly receipt pages with @media print CSS

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Backend | Spring Boot 2.7.18, Java 17+ |
| Database | MySQL 8.x with Spring Data JPA / Hibernate |
| Frontend | Thymeleaf, Bootstrap 5, Font Awesome 6 |
| Payments | PayPal Checkout SDK, M-Pesa Daraja API (OkHttp) |
| PDF Generation | iText html2pdf |
| Email | Spring Mail (JavaMailSender) |
| Security | Spring Security with BCrypt |
| Build Tool | Maven |

## Prerequisites

- **Java** 17 or higher (JDK 17+, compatible with JVM 23)
- **MySQL** 8.x
- **Maven** 3.6+
- **VS Code** with the following extensions recommended:
  - Extension Pack for Java
  - Spring Boot Extension Pack
  - Lombok Annotations Support

## Setup Instructions (Windows + VS Code)

### 1. Install Prerequisites

```bash
# Verify Java installation
java -version

# Verify Maven installation
mvn -version
```

### 2. Configure MySQL Database

```sql
-- Open MySQL command line or Workbench and run:
CREATE DATABASE tourismfx_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Clone and Configure

```bash
# Clone the repository
git clone <repository-url>
cd tourismfx

# Update database credentials in src/main/resources/application.properties
# Change these lines to match your MySQL setup:
#   spring.datasource.username=root
#   spring.datasource.password=root
```

### 4. Build and Run

```bash
# Build the project (downloads dependencies, compiles, runs tests)
mvn clean install

# Run the application
mvn spring-boot:run
```

### 5. Access the Application

Open your browser and navigate to: **http://localhost:8080**

### 6. Default Accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Tourist | tourist | tourist123 |

> **Note:** To use default accounts, initialize the database with `data.sql`. Set `spring.sql.init.mode=always` in `application.properties` and restart.

## Project Structure

```
tourismfx/
├── pom.xml                          # Maven dependencies
├── src/main/java/com/example/tourismfx/
│   ├── App.java                     # Application entry point
│   ├── config/
│   │   ├── JpaConfig.java           # JPA & auditing configuration
│   │   └── SecurityConfig.java      # Spring Security configuration
│   ├── model/                       # JPA entity classes
│   │   ├── User.java                # User authentication entity
│   │   ├── Tourist.java             # Tourist profile entity
│   │   ├── Hotel.java               # Hotel property entity
│   │   ├── Room.java                # Room type entity
│   │   ├── HotelBooking.java        # Booking record entity
│   │   ├── Payment.java             # Payment transaction entity
│   │   └── Receipt.java             # Receipt record entity
│   ├── repository/                  # Spring Data JPA repositories
│   ├── service/                     # Business logic layer
│   │   ├── UserService.java         # Authentication & registration
│   │   ├── HotelService.java        # Hotel & room management
│   │   ├── BookingService.java      # Booking lifecycle
│   │   ├── PaymentService.java      # Payment orchestration
│   │   └── ReceiptService.java      # PDF generation & email
│   ├── gateway/                     # Payment gateway abstraction
│   │   ├── PaymentGateway.java      # Gateway interface
│   │   └── MockPaymentGateway.java  # Dev/test mock implementation
│   └── view/                        # Spring MVC controllers
│       ├── LoginController.java
│       ├── RegistrationController.java
│       ├── AdminDashboardController.java
│       ├── TouristDashboardController.java
│       ├── HotelListController.java
│       ├── BookingController.java
│       ├── PaymentController.java
│       └── ReceiptController.java
├── src/main/resources/
│   ├── templates/                   # Thymeleaf HTML templates
│   ├── static/css/styles.css        # Futuristic dark theme CSS
│   ├── db/schema.sql                # Database schema (reference)
│   ├── db/data.sql                  # Seed data with sample hotels
│   └── application.properties       # Application configuration
└── src/test/java/                   # Unit tests
    ├── BookingServiceTest.java
    └── ReceiptServiceTest.java
```

## Payment Integration

### Mock Gateway (Default — Development)
The application uses `MockPaymentGateway` by default, which simulates payment processing without connecting to real APIs. All payments are automatically approved.

### PayPal (Production)
1. Create a PayPal Developer account at https://developer.paypal.com
2. Create an app to get Client ID and Secret
3. Implement `PayPalPaymentGateway` with real API calls
4. Use `@Profile("production")` annotation

### M-Pesa (Production)
1. Register at Safaricom Daraja portal: https://developer.safaricom.co.ke
2. Create an app to get Consumer Key and Secret
3. Implement `MpesaPaymentGateway` with Daraja STK Push API
4. Configure callback URL for payment notifications

## Email Configuration

For Gmail SMTP:
1. Enable 2-Factor Authentication on your Google account
2. Generate an App Password: Google Account → Security → App Passwords
3. Update `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BookingServiceTest
mvn test -Dtest=ReceiptServiceTest
```

## Development Tips (VS Code)

1. **Lombok Setup**: Install the Lombok VS Code extension or add Lombok to your VS Code Java settings
2. **Hot Reload**: `spring.thymeleaf.cache=false` is already set for template hot reload
3. **Debug**: Use VS Code's Java debugger with Spring Boot launch configuration
4. **Database Console**: Use MySQL Workbench or VS Code MySQL extension to view data

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -m "Add new feature"`)
4. Push to branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## License

This project is licensed under the MIT License.
