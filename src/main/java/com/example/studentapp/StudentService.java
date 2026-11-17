package com.example.studentapp;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentDAO dao;

    public StudentService(StudentDAO dao) {
        this.dao = dao;
    }

    public boolean checkHealth() {
        return dao.testConnection();
    }

    public List<Student> getAllStudents() throws SQLException {
        return dao.findAll();
    }

    public List<Student> searchStudents(String query) throws SQLException {
        if (query == null || query.isBlank()) {
            return dao.findAll();
        }
        return dao.search(query.trim());
    }

    public Optional<Student> getStudentByRegNo(String regNo) throws SQLException {
        if (regNo == null || regNo.isBlank()) {
            return Optional.empty();
        }
        return dao.findByRegNo(regNo.trim());
    }

    public void addStudent(String regNo, String name) throws SQLException, IllegalArgumentException {
        if (regNo == null || regNo.trim().isBlank()) {
            throw new IllegalArgumentException("Registration number is required.");
        }
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        String cleanReg = regNo.trim();
        String cleanName = name.trim();

        if (dao.findByRegNo(cleanReg).isPresent()) {
            throw new IllegalArgumentException("Duplicate registration number: " + cleanReg + " already exists.");
        }

        dao.insert(new Student(cleanReg, cleanName));
    }

    public boolean updateStudent(String regNo, String newName) throws SQLException, IllegalArgumentException {
        if (regNo == null || regNo.trim().isBlank()) {
            throw new IllegalArgumentException("Registration number is required.");
        }
        if (newName == null || newName.trim().isBlank()) {
            throw new IllegalArgumentException("Student name cannot be blank.");
        }
        return dao.updateName(regNo.trim(), newName.trim());
    }

    public boolean deleteStudent(String regNo) throws SQLException, IllegalArgumentException {
        if (regNo == null || regNo.trim().isBlank()) {
            throw new IllegalArgumentException("Registration number is required.");
        }
        return dao.delete(regNo.trim());
    }
}
