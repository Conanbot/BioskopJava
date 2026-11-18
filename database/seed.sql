USE bioskop_db;

SET @admin_username = 'admin';
SET @admin_password_hash = NULL;

INSERT INTO users (username, password_hash, role)
SELECT @admin_username, @admin_password_hash, 'ADMIN'
WHERE @admin_password_hash IS NOT NULL
  AND @admin_password_hash <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM users
      WHERE username = @admin_username
  );
