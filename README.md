# Sistem Booking Tiket Bioskop (BioskopJava)

Aplikasi Desktop Sistem Pemesanan Tiket Bioskop berbasis Java 17 dan Java Swing dengan arsitektur MVC, terintegrasi dengan MySQL 8 (Laragon / XAMPP) dan mendukung NetBeans GUI Builder serta Maven Build System.

---

## 1. Persyaratan Sistem & Lingkungan
- **JDK**: Java Development Kit 17 (LTS)
- **Build System**: Maven 3.9+ & NetBeans Ant Build System (`build.xml`)
- **IDE**: Apache NetBeans 17+
- **Database**: MySQL 8.0+ (Laragon / XAMPP)

---

## 2. Struktur Direktori Proyek

Struktur proyek telah diselaraskan dengan standar NetBeans Ant + Maven (`build.xml` + `pom.xml`):

```text
BioskopJava/
├── build.xml                 # NetBeans Ant build script
├── manifest.mf               # Application manifest
├── nbproject/                # NetBeans project configuration & metadata
│   ├── project.properties    # Konfigurasi source root, Java 17, & main class
│   ├── project.xml           # Definisi modul NetBeans J2SE
│   ├── build-impl.xml        # NetBeans Ant runtime tasks
│   └── genfiles.properties   # NetBeans build generation metadata
├── pom.xml                   # Maven single-module build configuration
├── README.md                 # Dokumentasi proyek
├── src/                      # Source code aplikasi (Java + NetBeans .form)
│   ├── Controler/            # Controllers, SessionManager, AlertUtil, DBConnection
│   ├── Model/                # DAO, Entity, Enums, Service, Payment, Exception
│   ├── View/                 # Swing views, Admin forms, Customer portal, Themes
│   └── mvcgui/               # Main entry point (MVCGUI.java)
├── test/                     # Unit test & MySQL integration test (JUnit 5)
│   ├── Controler/            # Controller & Session unit tests
│   └── Model/                # Model, DAO, Service, & Integration tests
└── database/                 # Skrip SQL database
    ├── schema.sql            # DDL skema database MySQL
    └── seed.sql              # DML data awal & akun default
```

---

## 3. Konfigurasi Koneksi Database

Koneksi database dikelola secara terpusat dan langsung melalui class:
**`src/Controler/DBConnection.java`**

Aplikasi **tidak lagi membutuhkan** file `config.properties` atau environment variable tambahan untuk penggunaan lokal standar.

- **Default Lingkungan (Laragon / XAMPP)**:
  - **Host**: `localhost`
  - **Port**: `3306`
  - **Database Aplikasi**: `bioskop_db`
  - **Database Testing**: `bioskop_test_db` (terisolasi untuk integration test)
  - **Username**: `root`
  - **Password**: *(kosong)*

---

## 4. Persiapan Database MySQL Laragon

1. Pastikan service **MySQL** pada Laragon / XAMPP sudah aktif.
2. Buka terminal atau GUI database (HeidiSQL / phpMyAdmin), lalu jalankan:
   - `database/schema.sql` (membuat database `bioskop_db` dan 7 tabel inti).
   - `database/seed.sql` (mengisi data awal studio, film, jadwal tayang, dan akun pengguna).
3. Untuk keperluan testing, jalankan juga skema pada database pengujian `bioskop_test_db`.

### Akun Bawaan (Default):
- **Admin**: `admin` / `admin`
- **Customer**: `user` / `user123`
- **Customer**: `dava` / `dava123`

*Catatan: Sistem menggunakan penyimpanan dan verifikasi password secara langsung (plain text) sesuai dengan kebutuhan tugas perkuliahan PBO.*

---

## 5. Cara Menjalankan Aplikasi

### Melalui Maven (Terminal):
```powershell
mvn compile exec:java
```
atau menggunakan binary Maven NetBeans:
```powershell
& "C:\Apps\NetBeans\netbeans\java\maven\bin\mvn.cmd" compile exec:java
```

### Melalui NetBeans IDE:
- Buka project **BioskopJava** di Apache NetBeans.
- Tekan **F6** atau klik kanan project → **Run**. Main class otomatis mengarah ke `mvcgui.MVCGUI`.

---

## 6. Pengujian Otomatis (Automated Testing)

Automated testing tersimpan di folder `test/` dan dijalankan melalui Maven Surefire (JUnit 5):

```powershell
mvn test
```
atau:
```powershell
mvn clean test
```

### Hasil Pengujian:
- **Total Test**: 50 tests.
- **Unit Tests**: Menguji logika bisnis, validasi konflik jadwal tayang (`ScheduleConflictValidationTest`), integritas kapasitas studio, otorisasi login, dan session management.
- **Integration Test (`MySqlIntegrationTest`)**: Terisolasi pada database `bioskop_test_db` sehingga aman dari modifikasi database utama.
