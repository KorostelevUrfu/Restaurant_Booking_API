package org.example.bookingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdatePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 50, message = "Password must be between 8 and 50 characters")
//    @Pattern(
//            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
//            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character"
//    )
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmNewPassword;

    public UpdatePasswordRequest() {}

    public UpdatePasswordRequest(String currentPassword, String newPassword, String confirmNewPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }

    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
