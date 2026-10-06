package com.hotelmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Guest {
    private Integer guestId;
    private String fullName;
    private String gender; // Male, Female, Other
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private IdProofType idProofType;
    private String idProofNumber;
    private String nationality;
    private LocalDateTime registrationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Guest() {}

    public Guest(Integer guestId, String fullName, String gender, LocalDate dateOfBirth, String phone,
                 String email, String address, IdProofType idProofType, String idProofNumber, String nationality) {
        this.guestId = guestId;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.nationality = nationality;
    }

    public Integer getGuestId() { return guestId; }
    public void setGuestId(Integer guestId) { this.guestId = guestId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public IdProofType getIdProofType() { return idProofType; }
    public void setIdProofType(IdProofType idProofType) { this.idProofType = idProofType; }

    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return fullName + " (ID: " + guestId + ", Phone: " + phone + ")";
    }
}
