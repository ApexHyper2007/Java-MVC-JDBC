package com.example.studentapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/school_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private final String url;
    private final String username;
    private final String password;

    public StudentDAO() {
        this.url = environmentOrDefault("SCHOOL_DB_URL", DEFAULT_URL);
        this.username = environmentOrDefault("SCHOOL_DB_USER", "root");
        this.password = System.getenv("SCHOOL_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Set the SCHOOL_DB_PASSWORD environment variable first.");
        }
    }

    public void insert(Student student) throws SQLException {
        // Strict insert so duplicate primary keys raise an exception to be handled by service
        String sql = "INSERT INTO students (reg_no, name) VALUES (?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.regNo());
            statement.setString(2, student.name());
            statement.executeUpdate();
        }
    }

    public Optional<Student> findByRegNo(String regNo) throws SQLException {
        String sql = "SELECT reg_no, name FROM students WHERE reg_no = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, regNo);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return Optional.of(new Student(
                            results.getString("reg_no"), results.getString("name")));
                }
                return Optional.empty();
            }
        }
    }

    public List<Student> findAll() throws SQLException {
        String sql = "SELECT reg_no, name FROM students ORDER BY reg_no";
        List<Student> students = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                students.add(new Student(
                        results.getString("reg_no"),
                        results.getString("name")));
            }
        }
        return students;
    }

    public List<Student> search(String keyword) throws SQLException {
        String sql = "SELECT reg_no, name FROM students WHERE LOWER(reg_no) LIKE LOWER(?) OR LOWER(name) LIKE LOWER(?) ORDER BY reg_no";
        List<Student> students = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    students.add(new Student(
                            results.getString("reg_no"),
                            results.getString("name")));
                }
            }
        }
        return students;
    }

    public boolean updateName(String regNo, String newName) throws SQLException {
        String sql = "UPDATE students SET name = ? WHERE reg_no = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newName);
            statement.setString(2, regNo);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(String regNo) throws SQLException {
        String sql = "DELETE FROM students WHERE reg_no = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, regNo);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean testConnection() {
        try (Connection connection = openConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
