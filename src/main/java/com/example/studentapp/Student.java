package com.example.studentapp;

public record Student(String registrationNumber, String name) {
    public Student {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new IllegalArgumentException("Registration number must not be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Student name must not be blank.");
        }
    }
}
