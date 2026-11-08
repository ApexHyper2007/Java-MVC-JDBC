# Student Management System (Java MVC + JDBC)

A modular, clean architecture Java console application demonstrating the **Model-View-Controller (MVC)** architectural pattern combined with **JDBC (Java Database Connectivity)** and **MySQL**.

---

## 🌟 Key Features & Queries

The application implements full CRUD and search operations using `PreparedStatement` to ensure security against SQL injection:

- **Create / Upsert**: Inserts new student records or updates them if duplicate (`ON DUPLICATE KEY UPDATE`).
- **Read (By ID)**: Searches student by registration number (`SELECT ... WHERE registration_number = ?`).
- **Read All**: Fetches all students ordered by registration number (`SELECT ... ORDER BY registration_number`).
- **Search (Fuzzy / Keyword)**: Case-insensitive search by student name using `LIKE %keyword%`.
- **Update**: Updates student name by registration number (`UPDATE students SET name = ? WHERE registration_number = ?`).
- **Delete**: Deletes records by registration number (`DELETE FROM students WHERE registration_number = ?`).
- **Aggregate Count**: Queries total number of students in the table (`SELECT COUNT(*) FROM students`).

---

## 🏗️ Architecture (MVC Pattern)

```text
├── Model (Student.java)               -> Immutable Java Record holding registration number & name
├── View (StudentView.java)            -> Console presentation layer for student details and status messages
├── Controller (StudentController.java)-> Orchestrates interactions between the View and DAO
├── DAO (StudentDAO.java)              -> Direct JDBC operations and MySQL query executions
└── Main (Main.java)                   -> Entry point simulating realistic CRUD operations
```

---

## 📂 Project Structure

```text
Java MVC + JDBC/
├── sql/
│   └── setup.sql              # Database & table creation schema
├── src/main/java/com/example/studentapp/
│   ├── Main.java              # Application driver
│   ├── Student.java           # Model (Java Record)
│   ├── StudentController.java # Controller
│   ├── StudentDAO.java        # Data Access Object (JDBC)
│   └── StudentView.java       # View layer
├── .gitignore                 # Excludes target/, .tools/, .m2/, etc.
├── pom.xml                    # Maven build configuration & dependencies
└── README.md                  # Project documentation
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **JDK 17+** (tested on JDK 25)
- **Apache Maven 3.9+**
- **MySQL Server 8.0+**

### 2. Database Setup
Make sure your MySQL server is running, then execute [`sql/setup.sql`](sql/setup.sql):

```sql
CREATE DATABASE IF NOT EXISTS school_db;
USE school_db;

CREATE TABLE IF NOT EXISTS students (
    registration_number VARCHAR(50) NOT NULL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);
```

Or via terminal:
```powershell
cmd /c "mysql -u root -p < sql\setup.sql"
```

### 3. Environment Variables
The application reads database credentials from environment variables:
- `SCHOOL_DB_PASSWORD` (Required): Your MySQL password.
- `SCHOOL_DB_USER` (Optional, defaults to `root`).
- `SCHOOL_DB_URL` (Optional, defaults to `jdbc:mysql://localhost:3306/school_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`).

To set the password in PowerShell:
```powershell
# For the current session:
$env:SCHOOL_DB_PASSWORD = "your_mysql_password"

# Or persistently for your user:
[Environment]::SetEnvironmentVariable('SCHOOL_DB_PASSWORD', 'your_mysql_password', 'User')
```

### 4. Build and Run

Using Maven:
```powershell
# Compile
mvn clean compile

# Run
mvn exec:java
```

*(If using the bundled local Maven in `.tools`)*:
```powershell
& ".\.tools\apache-maven-3.9.11\bin\mvn.cmd" "-Dmaven.repo.local=.m2\repository" clean compile
& ".\.tools\apache-maven-3.9.11\bin\mvn.cmd" "-Dmaven.repo.local=.m2\repository" exec:java
```

---

## 📋 Sample Output

```text
=== 1. Inserting Sample Students ===
  -> Registration No: REG-2026-001    | Name: Aarav Sharma
  -> Registration No: REG-2026-002    | Name: Meera Iyer
  -> Registration No: REG-2026-003    | Name: Rohan Verma

=== 2. Total Count Query ===
[INFO] Total student count in database: 3

=== 3. Find All Query ===
==================================================
  ALL REGISTERED STUDENTS
==================================================
  -> Registration No: REG-2026-001    | Name: Aarav Sharma
  -> Registration No: REG-2026-002    | Name: Meera Iyer
  -> Registration No: REG-2026-003    | Name: Rohan Verma

=== 4. Search By Name Query ===
==================================================
  SEARCH RESULTS FOR: 'Sharma'
==================================================
  -> Registration No: REG-2026-001    | Name: Aarav Sharma

=== 5. Update Query ===
[INFO] Successfully updated name for REG-2026-001 to 'Aarav K. Sharma'.

=== 6. Delete Query ===
[INFO] Successfully deleted student with registration number: REG-2026-003

=== 7. Final Database State ===
==================================================
  ALL REGISTERED STUDENTS
==================================================
  -> Registration No: REG-2026-001    | Name: Aarav K. Sharma
  -> Registration No: REG-2026-002    | Name: Meera Iyer

[INFO] Total student count in database: 2
```
