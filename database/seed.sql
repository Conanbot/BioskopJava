USE bioskop_db;

-- 1. Users (Password tanpa enkripsi sesuai permintaan modul perkuliahan PBO)
-- Username: admin / Password: admin (ADMIN)
-- Username: user / Password: user123 (CUSTOMER)
-- Username: dava / Password: dava123 (CUSTOMER)
INSERT INTO users (username, password_hash, role) VALUES
('admin', 'admin', 'ADMIN'),
('user', 'user123', 'CUSTOMER'),
('dava', 'dava123', 'CUSTOMER')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), role = VALUES(role);

-- 2. Studios
INSERT INTO studios (id, name, seat_rows, seat_cols) VALUES
(1, 'Studio 1 - Regular', 8, 10),
(2, 'Studio 2 - Premiere', 6, 8),
(3, 'Studio 3 - IMAX', 10, 12)
ON DUPLICATE KEY UPDATE name = VALUES(name), seat_rows = VALUES(seat_rows), seat_cols = VALUES(seat_cols);

-- 3. Films
INSERT INTO films (id, title, genre, duration_minutes, age_rating, synopsis, poster_path, status) VALUES
(1, 'Dune: Part Two', 'Sci-Fi / Adventure', 166, '13+', 'Paul Atreides bersatu dengan Chani dan Fremen untuk membalas dendam terhadap para konspirator yang menghancurkan keluarganya.', '', 'NOW_SHOWING'),
(2, 'Godzilla x Kong: The New Empire', 'Action / Sci-Fi', 115, '13+', 'Dua titan kuno, Godzilla dan Kong, bertarung bersama melawan ancaman kolosal tersembunyi di dunia Hollow Earth.', '', 'NOW_SHOWING'),
(3, 'Kung Fu Panda 4', 'Animation / Comedy', 94, 'SU', 'Po bersiap menjadi Pemimpin Spiritual Lembah Damai sambil mencari penerus Prajurit Naga baru.', '', 'NOW_SHOWING'),
(4, 'Siksa Kubur', 'Horror / Drama', 117, '17+', 'Sita meragukan konsep agama dan berniat mencari orang paling berdosa untuk membuktikan ketiadaan siksa kubur.', '', 'NOW_SHOWING'),
(5, 'Deadpool & Wolverine', 'Action / Comedy', 127, '17+', 'Wolverine yang enggan bergabung dengan Deadpool yang eksentrik untuk menyelamatkan multiverse.', '', 'COMING_SOON')
ON DUPLICATE KEY UPDATE title = VALUES(title), genre = VALUES(genre), duration_minutes = VALUES(duration_minutes), status = VALUES(status);

-- 4. Schedules (Hari ini dan besok)
INSERT INTO schedules (id, film_id, studio_id, show_date, show_time, ticket_price, total_seats, available_seats) VALUES
(1, 1, 1, CURDATE(), '13:00:00', 45000.00, 80, 80),
(2, 1, 1, CURDATE(), '16:30:00', 50000.00, 80, 80),
(3, 1, 3, CURDATE(), '19:30:00', 75000.00, 120, 120),
(4, 2, 2, CURDATE(), '14:00:00', 60000.00, 48, 48),
(5, 2, 2, CURDATE(), '17:00:00', 60000.00, 48, 48),
(6, 3, 1, CURDATE(), '10:30:00', 40000.00, 80, 80),
(7, 4, 1, CURDATE(), '20:00:00', 50000.00, 80, 80),
(8, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00', 45000.00, 80, 80),
(9, 2, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00', 60000.00, 48, 48)
ON DUPLICATE KEY UPDATE ticket_price = VALUES(ticket_price), total_seats = VALUES(total_seats);
