CREATE DATABASE IF NOT EXISTS bioskop_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE bioskop_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS films (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    genre VARCHAR(80) NOT NULL,
    duration_minutes SMALLINT UNSIGNED NOT NULL,
    age_rating VARCHAR(10) NOT NULL,
    synopsis TEXT NOT NULL,
    poster_path VARCHAR(500),
    status ENUM('NOW_SHOWING', 'COMING_SOON', 'ARCHIVED') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_films_duration CHECK (duration_minutes > 0),
    KEY idx_films_status_title (status, title)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS studios (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    seat_rows SMALLINT UNSIGNED NOT NULL,
    seat_cols SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_studios_name (name),
    CONSTRAINT chk_studios_seat_rows CHECK (seat_rows > 0),
    CONSTRAINT chk_studios_seat_cols CHECK (seat_cols > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    film_id BIGINT UNSIGNED NOT NULL,
    studio_id BIGINT UNSIGNED NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    ticket_price DECIMAL(10, 2) NOT NULL,
    total_seats SMALLINT UNSIGNED NOT NULL,
    available_seats SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_schedules_studio_start (studio_id, show_date, show_time),
    KEY idx_schedules_film_start (film_id, show_date, show_time),
    CONSTRAINT fk_schedules_film
        FOREIGN KEY (film_id) REFERENCES films (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_schedules_studio
        FOREIGN KEY (studio_id) REFERENCES studios (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_schedules_ticket_price CHECK (ticket_price > 0),
    CONSTRAINT chk_schedules_total_seats CHECK (total_seats > 0),
    CONSTRAINT chk_schedules_available_seats
        CHECK (available_seats <= total_seats)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    booking_code VARCHAR(24) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    schedule_id BIGINT UNSIGNED NOT NULL,
    status ENUM('ACTIVE', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'ACTIVE',
    ticket_count TINYINT UNSIGNED NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_bookings_code (booking_code),
    UNIQUE KEY uq_bookings_id_schedule (id, schedule_id),
    KEY idx_bookings_user_created (user_id, created_at),
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_schedule
        FOREIGN KEY (schedule_id) REFERENCES schedules (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bookings_ticket_count CHECK (ticket_count BETWEEN 1 AND 6),
    CONSTRAINT chk_bookings_total_price CHECK (total_price > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS booking_seats (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    booking_id BIGINT UNSIGNED NOT NULL,
    schedule_id BIGINT UNSIGNED NOT NULL,
    seat_code VARCHAR(8) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_booking_seats_schedule_seat (schedule_id, seat_code),
    KEY idx_booking_seats_booking (booking_id),
    CONSTRAINT fk_booking_seats_booking_schedule
        FOREIGN KEY (booking_id, schedule_id)
        REFERENCES bookings (id, schedule_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_booking_seats_code CHECK (CHAR_LENGTH(seat_code) > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    booking_id BIGINT UNSIGNED NOT NULL,
    method ENUM('CASH', 'QRIS') NOT NULL,
    status ENUM('PAID', 'REFUNDED') NOT NULL DEFAULT 'PAID',
    amount_paid DECIMAL(10, 2) NOT NULL,
    cash_received DECIMAL(10, 2) NOT NULL,
    change_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refunded_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payments_booking (booking_id),
    CONSTRAINT fk_payments_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_payments_amount_paid CHECK (amount_paid > 0),
    CONSTRAINT chk_payments_change_amount CHECK (change_amount >= 0),
    CONSTRAINT chk_payments_method_amounts CHECK (
        (method = 'CASH'
            AND cash_received IS NOT NULL
            AND cash_received >= amount_paid
            AND change_amount = cash_received - amount_paid)
        OR
        (method = 'QRIS'
            AND cash_received = 0
            AND change_amount = 0)
    ),
    CONSTRAINT chk_payments_refund_time CHECK (
        (status = 'PAID' AND refunded_at IS NULL)
        OR (status = 'REFUNDED' AND refunded_at IS NOT NULL)
    )
) ENGINE = InnoDB;
