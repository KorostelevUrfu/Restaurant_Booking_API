package org.example.bookingsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateRequest {
    @Pattern(regexp = "^[А-Яа-яA-Za-z-]+$", message = "Last name must contain only letters")
    private String lastName;

    @Pattern(regexp = "^[А-Яа-яA-Za-z-]+$", message = "First name must contain only letters")
    private String firstName;

    //проверка отчества происходит в UserService.checkAvailability так как @Pattern не разрешает null
    private String middleName;

    @Pattern(regexp = "^\\+7\\d{10}$", message = "Phone must be in format +7XXXXXXXXXX")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    public UpdateRequest(){}

    public UpdateRequest(String lastName, String firstName, String middleName, String phone, String email) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.phone = phone;
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
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
}
