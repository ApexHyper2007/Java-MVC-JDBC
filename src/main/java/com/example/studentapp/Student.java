package com.example.studentapp;

public record Student(String regNo, String name) {
    public Student {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException("Registration number must not be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Student name must not be blank.");
        }
    }
}
