package Model.entity;

import Model.enums.Role;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entitas pengguna yang sesuai dengan tabel users di database.
 * Password hash tidak disertakan untuk mencegah kebocoran informasi sensitif.
 */
public final class User {
    private final long id;
    private final String username;
    private final Role role;
    private final LocalDateTime createdAt;

    public User(long id, String username, Role role, LocalDateTime createdAt) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID pengguna harus positif.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username wajib diisi.");
        }
        this.id = id;
        this.username = username;
        this.role = Objects.requireNonNull(role, "Role pengguna wajib diisi.");
        this.createdAt = Objects.requireNonNull(createdAt, "Waktu pembuatan wajib diisi.");
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}
