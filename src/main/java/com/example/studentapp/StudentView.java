package com.example.studentapp;

import java.util.List;

public class StudentView {
    public void displayStudent(Student student) {
        System.out.printf("  -> Registration No: %-15s | Name: %s%n",
                student.registrationNumber(), student.name());
    }

    public void displayStudentList(String title, List<Student> students) {
        System.out.println("==================================================");
        System.out.println("  " + title);
        System.out.println("==================================================");
        if (students.isEmpty()) {
            System.out.println("  (No records found)");
        } else {
            for (Student student : students) {
                displayStudent(student);
            }
        }
        System.out.println();
    }

    public void displayMessage(String message) {
        System.out.println("[INFO] " + message);
    }
}
