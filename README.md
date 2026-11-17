# Student Management System (Python Tkinter Frontend + Java MVC, JDBC & MySQL)

**Course**: 21CSC203P - Advanced Programming Practice  
**Institution**: SRM Institute of Science and Technology, Kattankulathur  
**Student**: Tanishk Kumar Yadav (RA2511003011310, B.Tech CSE Core B2)

---

## 🌟 Overview & Architecture

The application implements a desktop **Student Management System** split into a modern **Python Tkinter Desktop GUI** frontend and a modular **Java MVC + JDBC** REST API backend communicating via local HTTP/JSON:

```text
┌────────────────────────────────────────────────────────┐
│               Python Tkinter Frontend                  │
│   (Form Entries, CRUD Buttons, Treeview Table, Search) │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP / JSON (localhost:8080)
┌───────────────────────────▼────────────────────────────┐
│                    Java Controller                     │
│           (Maps HTTP endpoints to Java service)        │
└───────────────────────────┬────────────────────────────┘
                            │ Method calls
┌───────────────────────────▼────────────────────────────┐
│                     Service Layer                      │
│        (Validates input, checks duplicate reg_no)      │
└───────────────────────────┬────────────────────────────┘
                            │ DAO calls
┌───────────────────────────▼────────────────────────────┐
│                      Student DAO                       │
│       (Executes parameterized SQL queries via JDBC)     │
└───────────────────────────┬────────────────────────────┘
                            │ JDBC connection
┌───────────────────────────▼────────────────────────────┐
│                    MySQL Database                      │
│          Database: school_db | Table: students         │
└────────────────────────────────────────────────────────┘
```

---

## 📂 Project Structure

```text
Java MVC + JDBC/
├── app.py                     # Python Tkinter Desktop Frontend
├── sql/
│   └── setup.sql              # Database & table creation schema (reg_no, name)
├── src/main/java/com/example/studentapp/
│   ├── Main.java              # Java HTTP Server entry point (Port 8080)
│   ├── Student.java           # Model record (regNo, name)
│   ├── StudentController.java # HTTP Controller for REST API endpoints
│   ├── StudentService.java    # Service Layer with validation & business logic
│   ├── StudentDAO.java        # Data Access Object executing JDBC queries
│   └── StudentView.java       # Console output formatter
├── pom.xml                    # Maven build configuration & MySQL driver
├── .gitignore                 # Ignores target/, .tools/, .m2/, *.pyc
└── README.md                  # Project documentation
```

---

## 🔌 API Operations

| HTTP Method & Endpoint | Operation | Description |
| :--- | :--- | :--- |
| `GET /api/health` | Health Check | Verifies Java backend and MySQL database connectivity |
| `GET /api/students` | Get All Students | Retrieves all student records |
| `GET /api/students?q={term}` | Search | Case-insensitive search on registration number and name |
| `POST /api/students` | Insert Student | Creates a new student record (rejects duplicate `reg_no`) |
| `PUT /api/students/{reg_no}` | Update Student | Updates student name for the given registration number |
| `DELETE /api/students/{reg_no}` | Delete Student | Deletes student record by registration number |

---

## 🗄️ Database Design

**Database**: `school_db`  
**Table**: `students`

| Field | Data Type | Constraint / Purpose |
| :--- | :--- | :--- |
| `reg_no` | `VARCHAR(30)` | Primary Key; Unique student registration number |
| `name` | `VARCHAR(100)` | NOT NULL; Student name |

To initialize or verify:
```sql
CREATE DATABASE IF NOT EXISTS school_db;
USE school_db;
CREATE TABLE IF NOT EXISTS students (
    reg_no VARCHAR(30) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
```

---

## 🚀 How to Run the Application

### Step 1: Ensure MySQL Server is Running
Start your MySQL Server (via Windows Services `MySQL80`, MySQL Workbench, or XAMPP).

### Step 2: Start the Java Backend
Open a terminal in the project root:

```powershell
# 1. Set your MySQL credentials
$env:SCHOOL_DB_PASSWORD = "your_mysql_password"

# 2. Build and launch the Java backend server
& ".\.tools\apache-maven-3.9.11\bin\mvn.cmd" "-Dmaven.repo.local=.m2\repository" clean compile
& ".\.tools\apache-maven-3.9.11\bin\mvn.cmd" "-Dmaven.repo.local=.m2\repository" exec:java
```
*(Or use standard `mvn clean compile` and `mvn exec:java` if Maven is in your PATH).*

You should see:
```text
[INFO] Student API server listening on http://localhost:8080
==================================================
 Student Management Backend is running!
 API URL: http://localhost:8080/api/students
 Health Check: http://localhost:8080/api/health
==================================================
```

### Step 3: Launch the Python Tkinter Frontend
Open a **second terminal** window in the same folder and run:

```powershell
python app.py
```

The Tkinter GUI will open, display your database connection status at the bottom, and allow you to Add, Update, Delete, Search, and Refresh student records with instant GUI feedback.
