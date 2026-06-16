# Library Management System

A complete Library Management System built with Java 17, Maven, JDBC, and HikariCP. This application manages books, members, and borrowing operations with a console-based interface.

## Technologies Used

- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: H2 (embedded) / PostgreSQL (production)
- **Connection Pool**: HikariCP
- **Logging**: SLF4J with Logback
- **Testing**: JUnit 5, Mockito

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL (optional, for production)

## Project Structure

```
library-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/library/
│   │   │   ├── entity/          # Entity classes (Book, Member, BorrowRecord)
│   │   │   ├── repository/      # Repository interfaces and JDBC implementations
│   │   │   ├── service/         # Service interfaces and business logic implementations
│   │   │   ├── exception/       # Custom exception classes
│   │   │   ├── ui/              # Console-based user interface
│   │   │   ├── util/            # Database connection utility
│   │   │   └── Main.java        # Application entry point
│   │   └── resources/
│   │       ├── database.properties    # Database configuration
│   │       ├── logback.xml           # Logging configuration
│   │       └── db/
│   │           ├── h2/              # H2 database scripts
│   │           └── postgresql/      # PostgreSQL database scripts
│   └── test/
│       └── java/com/library/service/  # Service layer tests
└── pom.xml
```

## Setup Instructions

### 1. Clone the repository
```bash
git clone <repository-url>
cd library-management-system
```

### 2. Configure database
Edit `src/main/resources/database.properties` to configure your database connection:
- For H2 (default): No additional setup needed
- For PostgreSQL: Update the JDBC URL, username, and password

### 3. Initialize database
Run the appropriate schema and data scripts:
- H2: Execute `src/main/resources/db/h2/schema.sql` and `data.sql`
- PostgreSQL: Execute `src/main/resources/db/postgresql/schema.sql` and `data.sql`

### 4. Build the project
```bash
mvn clean compile
```

### 5. Run the application
```bash
mvn exec:java -Dexec.mainClass="com.library.Main"
```

## Features

### Book Management
- Add, update, delete books
- Search books by title, author, or ISBN
- View all books with details

### Member Management
- Register, update, delete members
- Search members by name or national code
- View all registered members

### Borrowing System
- Borrow books with validation
- Return books with overdue detection
- View borrowing history
- View active borrows and overdue books

## Business Rules

- Maximum 3 active borrows per member
- Maximum borrow period: 14 days
- Overdue warning for returns after 14 days
- ISBN uniqueness enforcement
- National code uniqueness enforcement

## Database Schema

### Books Table
- id (BIGINT, PRIMARY KEY)
- title (VARCHAR(255), NOT NULL)
- author (VARCHAR(255), NOT NULL)
- isbn (VARCHAR(20), UNIQUE)
- published_year (INTEGER)
- quantity (INTEGER)
- available_quantity (INTEGER)

### Members Table
- id (BIGINT, PRIMARY KEY)
- name (VARCHAR(255), NOT NULL)
- national_code (VARCHAR(20), UNIQUE)
- phone_number (VARCHAR(20))
- join_date (DATE)

### Borrow Records Table
- id (BIGINT, PRIMARY KEY)
- book_id (BIGINT, FOREIGN KEY)
- member_id (BIGINT, FOREIGN KEY)
- borrow_date (DATE)
- return_date (DATE)
- status (VARCHAR(20): BORROWED, RETURNED, OVERDUE)

## Testing

Run unit tests:
```bash
mvn test
```

## Troubleshooting

1. **Database connection issues**: Verify database.properties configuration
2. **Port conflicts**: Ensure PostgreSQL is running on the configured port
3. **Memory issues**: Increase JVM heap size if needed: `-Xmx512m`

## Future Improvements

- Web-based UI (REST API + frontend)
- User authentication and authorization
- Email notifications for overdue books
- Book reservation system
- Fine calculation and payment tracking
- Barcode/QR code integration
- Audit logging

## Author

Library Management System - Built with Java and passion for clean code.

## License

This project is open source and available under the MIT License.