# Sistem Booking Tiket Bioskop

Project foundation for a Java 17 and Java Swing cinema ticket booking system. The shared database schema, connection manager, booking/payment services, and startup diagnostic screen are implemented; the full booking UI and other team-owned modules are still in progress.

## Requirements

- JDK 17
- Maven 3.9+
- MySQL 8.0.16+

## Project structure

The source folders follow the supplied MVC reference: `src/Controler` contains controllers and connection utilities, `src/Model` contains booking, DAO, service, and payment classes, `src/View` contains Swing screens and theme resources (including `MainFrame`), and `src/mvcgui` contains the `MVCGUI` entry point. The Maven build uses these folders directly. Database scripts are in `database/`.

## Local database setup

1. Run `database/schema.sql` in MySQL.
2. Copy `config.properties.example` to `config.properties`.
3. Set `db.url`, `db.username`, and `db.password` in `config.properties`. The local configuration file is ignored by Git. Alternatively, set `BIOSKOP_DB_URL`, `BIOSKOP_DB_USERNAME`, and `BIOSKOP_DB_PASSWORD`.
4. Run `mvn compile` to compile the application.
5. Run `mvn exec:java` to open the startup diagnostic screen.

On the startup screen, use **Periksa koneksi MySQL** to verify the configured connection, required tables, and seat uniqueness index. **Uji dialog** opens a sample informational dialog.

The Swing interface is built programmatically in Java. It does not depend on NetBeans GUI Builder or `.form` files.

This minimal distribution intentionally excludes `target/`, local `config.properties`, automated tests, and extended technical documents. Test and documentation backups are maintained separately. Only `config.properties.example` is included; copy it to `config.properties` locally and never distribute the local file.

`DBConnection` is a singleton connection manager, not a shared JDBC connection. Each call to `getConnection()` returns a new connection, which must be closed by its caller.

To add the initial admin account, set `@admin_password_hash` in `database/seed.sql` to a BCrypt hash before running it. The seed leaves it unset so a placeholder password cannot be used accidentally.

The current QRIS and CASH flow is a local simulation; no payment gateway is connected.
