package com.example.studentapp;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Student> sampleStudents = List.of(
                new Student("REG-2026-001", "Aarav Sharma"),
                new Student("REG-2026-002", "Meera Iyer"),
                new Student("REG-2026-003", "Rohan Verma"));

        try {
            StudentController controller = new StudentController(new StudentDAO(), new StudentView());

            System.out.println("=== 1. Inserting Sample Students ===");
            for (Student student : sampleStudents) {
                controller.addAndDisplay(student);
            }

            System.out.println("\n=== 2. Total Count Query ===");
            controller.displayTotalCount();

            System.out.println("=== 3. Find All Query ===");
            controller.displayAllStudents();

            System.out.println("=== 4. Search By Name Query ===");
            controller.searchByName("Sharma");

            System.out.println("=== 5. Update Query ===");
            controller.updateStudentName("REG-2026-001", "Aarav K. Sharma");

            System.out.println("\n=== 6. Delete Query ===");
            controller.deleteStudent("REG-2026-003");

            System.out.println("\n=== 7. Final Database State ===");
            controller.displayAllStudents();
            controller.displayTotalCount();

        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Student application failed: " + exception.getMessage());
            System.exit(1);
        }
    }
}
