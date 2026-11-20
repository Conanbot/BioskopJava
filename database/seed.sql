USE bioskop_db;

-- Pastikan koneksi menggunakan collation yang sama dengan tabel (utf8mb4_0900_ai_ci)
-- agar perbandingan string pada WHERE username = ... tidak menghasilkan error #1267.
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- =============================================================================
-- SEED DATA & PROVISIONING AKUN ADMIN
-- =============================================================================
-- Catatan Keamanan:
-- Password admin TIDAK BOLEH ditulis dalam plaintext di file source code atau SQL.
--
-- Cara menjalankan seed ini dengan benar (lewat phpMyAdmin):
--   1. Jalankan AdminProvisioningService via Java untuk mendapatkan BCrypt hash:
--        java -cp "target/classes;..." Model.service.AdminProvisioningService admin <password>
--   2. Salin hash yang dihasilkan (contoh: $2a$12$xxx...)
--   3. Jalankan dua query berikut di phpMyAdmin SEBELUM mengimpor seed.sql:
--        SET @admin_username = 'admin';
--        SET @admin_password_hash = '$2a$12$hash_yang_disalin_di_sini';
--   4. Lalu impor file seed.sql ini.
--
-- Atau gunakan Java CLI langsung (lebih mudah, tidak perlu seed.sql untuk admin):
--   java -cp "target/classes;C:\Users\<user>\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar;C:\Users\<user>\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar" Model.service.AdminProvisioningService admin <password>
-- =============================================================================

INSERT INTO users (username, password_hash, role)
SELECT
    COALESCE(@admin_username, 'admin') COLLATE utf8mb4_0900_ai_ci,
    @admin_password_hash,
    'ADMIN'
WHERE @admin_password_hash IS NOT NULL
  AND @admin_password_hash <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM users
      WHERE username = COALESCE(@admin_username, 'admin') COLLATE utf8mb4_0900_ai_ci
  );
