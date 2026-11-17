package com.example.studentapp;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {}
        }

        try {
            StudentDAO dao = new StudentDAO();
            StudentService service = new StudentService(dao);
            StudentController controller = new StudentController(service);

            controller.start(port);
            System.out.println("==================================================");
            System.out.println(" Student Management Backend is running!");
            System.out.println(" API URL: http://localhost:" + port + "/api/students");
            System.out.println(" Health Check: http://localhost:" + port + "/api/health");
            System.out.println(" Press Ctrl+C in this terminal to stop the server.");
            System.out.println("==================================================");

            // Keep main thread alive
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            System.out.println("[INFO] Server stopped.");
        } catch (Exception exception) {
            System.err.println("Student backend application failed to start: " + exception.getMessage());
            System.exit(1);
        }
    }
}
