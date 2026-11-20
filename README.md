# Sistem Booking Tiket Bioskop

Project foundation for a Java 17 and Java Swing cinema ticket booking system. The shared database schema, connection manager, booking/payment services, admin operations (dashboard, studio management, schedule management with interval conflict detection, cancel/refund), user authentication, and diagnostic screens are fully implemented and integrated.

## Requirements

- JDK 17
- Maven 3.9+
- MySQL 8.0.16+

## Project Structure

The project follows the standard MVC architecture:
- `src/Controler`: Controllers, navigation, session management, and database connection utilities (`DBConnection`, `ConnectionProvider`).
- `src/Model`: Entity definitions, Data Access Objects (`UserDao`, `StudioDao`, `ScheduleDao`, `BookingDao`), business services (`AuthService`, `BookingService`, `AdminProvisioningService`, `DatabaseHealthService`), and domain exceptions.
- `src/View`: Java Swing user interface components and theme resources (`SwingTheme`, `MainFrame`, `AdminPanel`, `ScheduleManagementPanel`, `StudioManagementPanel`, `AdminDashboardPanel`, `AccountsPanel`, `StartupPanel`).
- `src/mvcgui`: Application main entry point (`MVCGUI`).
- `database/`: Database DDL schema (`schema.sql`) and initial seed script (`seed.sql`).
- `tests/`: Automated unit and integration test suite using JUnit 5.

The Swing interface is built entirely in standard programmatic Java without dependency on NetBeans GUI Builder or `.form` files.

## Database Configuration

1. Execute `database/schema.sql` in MySQL to initialize `bioskop_db` and the core tables (`users`, `films`, `studios`, `schedules`, `bookings`, `booking_seats`, `payments`).
2. Copy `config.properties.example` to `config.properties`.
3. Configure your MySQL credentials in `config.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/bioskop_db
   db.username=root
   db.password=your_mysql_password
   ```
   *Note: `config.properties` is ignored by Git and must never be committed.*
4. Alternatively, configure via environment variables:
   - `BIOSKOP_DB_URL`
   - `BIOSKOP_DB_USERNAME`
   - `BIOSKOP_DB_PASSWORD`

`DBConnection` acts as a connection manager; each invocation of `getConnection()` opens a new `Connection` that should be closed by the caller using try-with-resources.

## Admin Provisioning

Akun Administrator dibuat secara langsung pada database (menggunakan username dan password teks biasa sesuai materi pembelajaran PBO). Mekanisme ini bersifat idempoten (dapat dijalankan berulang tanpa membuat akun duplikat).

### Opsi 1: Melalui database/seed.sql (Paling Mudah)

Cukup import file `database/seed.sql` melalui phpMyAdmin atau MySQL CLI. Akun default telah disediakan:
- **Admin**: `admin` / `admin`
- **Customer**: `user` / `user123`
- **Customer**: `dava` / `dava123`

## Running the Application

1. Compile the project:
   ```bash
   mvn clean compile
   ```
2. Run the application:
   ```bash
   mvn exec:java
   ```
3. Or package into an executable JAR:
   ```bash
   mvn clean package
   java -jar target/BioskopJava-1.0-SNAPSHOT.jar
   ```

On the startup diagnostic screen, use **Periksa koneksi MySQL** to verify connectivity, the 7 core tables, and constraints. Navigate to **Akun dan sesi** to login as ADMIN or register as CUSTOMER.

## Automated Testing

Automated testing is maintained in the `tests/` directory and executed using Maven Surefire with JUnit 5.

### Menjalankan Test Suite

Jalankan seluruh test:
```bash
mvn test
```

Atau jalankan clean test:
```bash
mvn clean test
```

### Cakupan Pengujian (Test Coverage)

Test suite mencakup pengujian unit dan validasi domain (44 test cases):
1. **Model.ScheduleConflictValidationTest**: Validasi konflik jadwal berbasis interval waktu penayangan film (`existing_start < new_end AND existing_end > new_start`), jadwal berurutan tanpa konflik, jadwal studio berbeda, tanggal berbeda, update jadwal tanpa false conflict, serta variasi durasi film.
2. **Model.StudioCapacityConsistencyTest**: Validasi pencegahan pengubahan dimensi studio yang sudah digunakan jadwal, izin pengubahan untuk studio belum digunakan, sinkronisasi `total_seats` dan `available_seats`, serta pencegahan penurunan kapasitas di bawah kursi yang telah dipesan.
3. **Model.ScheduleDeletionValidationTest**: Validasi proteksi integritas FK; pemblokiran penghapusan jadwal yang memiliki riwayat booking dalam status apa pun (`ACTIVE`, `CANCELLED`, `REFUNDED`), dan keberhasilan penghapusan jadwal tanpa transaksi.
4. **Model.AdminProvisioningTest**: Pembuatan akun admin, verifikasi password, sifat idempoten provisioning berulang, dan validasi kredensial.
5. **Model.DashboardSummaryTest**: Validasi kelengkapan 4 metrik dashboard admin (Total Film, Total Jadwal Hari Ini, Total Booking, Total Pendapatan) dan validasi non-negatif.
6. **Model.service.AuthServiceTest**: Validasi registrasi customer, penyimpanan password, dan verifikasi login admin/customer.
7. **Controler.SessionManagerTest**: Manajemen sesi login/logout dan otorisasi role.
8. **Model.entity.StudioScheduleModelTest**: Validasi domain entity Studio dan Schedule.
9. **Model.integration.MySqlIntegrationTest**: Pengujian konektivitas live database MySQL (secara aman di-skip jika MySQL server lokal tidak aktif).
