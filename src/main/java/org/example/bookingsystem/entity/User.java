package org.example.bookingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String firstName;

    private String middleName;

    @Column(unique = true)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    private String role;

    private LocalDateTime createdAt;

    //активна учетка или нет(нет - "удалена")
    private boolean isActive;

    @Column(nullable = false)
    private int failedAttempt;

    private LocalDateTime temporaryBan;

    public User(){}

    public User(String login, String passwordHash, String lastName, String firstName, String middleName, String phone, String email) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.phone = phone;
        this.email = email;
        this.role = "client";
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.failedAttempt = 0;
        this.temporaryBan = null;
        this.publicId = UUID.randomUUID();
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive){
        this.isActive = isActive;
    }

    public int getFailedAttempt() {
        return failedAttempt;
    }

    public void setFailedAttempt(int failedAttempt) {
        this.failedAttempt = failedAttempt;
    }

    public LocalDateTime getTemporaryBan() {
        return temporaryBan;
    }

    public void setTemporaryBan(LocalDateTime temporaryBan) {
        this.temporaryBan = temporaryBan;
    }

    public UUID getPublicId() {
        return publicId;
    }
}
