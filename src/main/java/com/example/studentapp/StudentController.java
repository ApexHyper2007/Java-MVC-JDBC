package com.example.studentapp;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StudentController {
    private final StudentDAO dao;
    private final StudentView view;

    public StudentController(StudentDAO dao, StudentView view) {
        this.dao = dao;
        this.view = view;
    }

    public void addAndDisplay(Student student) throws SQLException {
        dao.insert(student);
        Optional<Student> saved = dao.findByRegistrationNumber(student.registrationNumber());
        if (saved.isPresent()) {
            view.displayStudent(saved.get());
        } else {
            view.displayMessage("No student found for registration number: " + student.registrationNumber());
        }
    }

    public void displayAllStudents() throws SQLException {
        List<Student> students = dao.findAll();
        view.displayStudentList("ALL REGISTERED STUDENTS", students);
    }

    public void searchByName(String keyword) throws SQLException {
        List<Student> results = dao.searchByName(keyword);
        view.displayStudentList("SEARCH RESULTS FOR: '" + keyword + "'", results);
    }

    public void updateStudentName(String regNo, String newName) throws SQLException {
        boolean updated = dao.updateName(regNo, newName);
        if (updated) {
            view.displayMessage("Successfully updated name for " + regNo + " to '" + newName + "'.");
        } else {
            view.displayMessage("Update failed: No student found with registration number " + regNo + ".");
        }
    }

    public void deleteStudent(String regNo) throws SQLException {
        boolean deleted = dao.delete(regNo);
        if (deleted) {
            view.displayMessage("Successfully deleted student with registration number: " + regNo);
        } else {
            view.displayMessage("Delete failed: No student found with registration number " + regNo + ".");
        }
    }

    public void displayTotalCount() throws SQLException {
        int count = dao.count();
        view.displayMessage("Total student count in database: " + count);
    }
}
