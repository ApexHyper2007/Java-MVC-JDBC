package com.example.studentapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentController {
    private final StudentService service;
    private HttpServer server;

    public StudentController(StudentService service) {
        this.service = service;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/students", new StudentsHandler());
        server.setExecutor(null); // default executor
        server.start();
        System.out.println("[INFO] Student API server listening on http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                boolean healthy = service.checkHealth();
                if (healthy) {
                    sendResponse(exchange, 200, "{\"status\":\"UP\",\"database\":\"connected\"}");
                } else {
                    sendResponse(exchange, 503, "{\"status\":\"DOWN\",\"database\":\"disconnected\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();

            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 204, "");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            // Path can be /api/students or /api/students/{reg_no}
            String pathSuffix = path.replaceFirst("^/api/students/?", "");
            String regNoFromPath = pathSuffix.isBlank() ? null : URLDecoder.decode(pathSuffix, StandardCharsets.UTF_8);

            try {
                switch (method) {
                    case "GET" -> handleGet(exchange, regNoFromPath);
                    case "POST" -> handlePost(exchange);
                    case "PUT" -> handlePut(exchange, regNoFromPath);
                    case "DELETE" -> handleDelete(exchange, regNoFromPath);
                    default -> sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (IllegalArgumentException e) {
                sendResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\":\"Database error: " + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"Internal server error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }

        private void handleGet(HttpExchange exchange, String regNoFromPath) throws IOException, SQLException {
            if (regNoFromPath != null && !regNoFromPath.isBlank()) {
                Optional<Student> student = service.getStudentByRegNo(regNoFromPath);
                if (student.isPresent()) {
                    sendResponse(exchange, 200, studentToJson(student.get()));
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Student not found\"}");
                }
                return;
            }

            // Check query param for search: ?q=term
            String query = exchange.getRequestURI().getQuery();
            String searchTerm = null;
            if (query != null && !query.isBlank()) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2 && "q".equalsIgnoreCase(pair[0])) {
                        searchTerm = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        break;
                    }
                }
            }

            List<Student> students = (searchTerm != null)
                    ? service.searchStudents(searchTerm)
                    : service.getAllStudents();

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < students.size(); i++) {
                sb.append(studentToJson(students.get(i)));
                if (i < students.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            sendResponse(exchange, 200, sb.toString());
        }

        private void handlePost(HttpExchange exchange) throws IOException, SQLException {
            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);

            String regNo = json.get("reg_no");
            if (regNo == null) regNo = json.get("regNo");
            String name = json.get("name");

            service.addStudent(regNo, name);
            sendResponse(exchange, 201, "{\"message\":\"Student added successfully\",\"reg_no\":\""
                    + escapeJson(regNo) + "\",\"name\":\"" + escapeJson(name) + "\"}");
        }

        private void handlePut(HttpExchange exchange, String regNoFromPath) throws IOException, SQLException {
            if (regNoFromPath == null || regNoFromPath.isBlank()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing registration number in URL path\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            String name = json.get("name");

            boolean updated = service.updateStudent(regNoFromPath, name);
            if (updated) {
                sendResponse(exchange, 200, "{\"message\":\"Student updated successfully\",\"reg_no\":\""
                        + escapeJson(regNoFromPath) + "\",\"name\":\"" + escapeJson(name) + "\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Student not found with reg_no: " + escapeJson(regNoFromPath) + "\"}");
            }
        }

        private void handleDelete(HttpExchange exchange, String regNoFromPath) throws IOException, SQLException {
            if (regNoFromPath == null || regNoFromPath.isBlank()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing registration number in URL path\"}");
                return;
            }

            boolean deleted = service.deleteStudent(regNoFromPath);
            if (deleted) {
                sendResponse(exchange, 200, "{\"message\":\"Student deleted successfully\",\"reg_no\":\""
                        + escapeJson(regNoFromPath) + "\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Student not found with reg_no: " + escapeJson(regNoFromPath) + "\"}");
            }
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        // Split on comma not inside quotes
        String[] entries = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String entry : entries) {
            String[] kv = entry.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String key = cleanJsonToken(kv[0]);
                String val = cleanJsonToken(kv[1]);
                map.put(key, val);
            }
        }
        return map;
    }

    private static String cleanJsonToken(String token) {
        String t = token.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1);
        }
        return t.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String studentToJson(Student s) {
        return "{\"reg_no\":\"" + escapeJson(s.regNo()) + "\",\"name\":\"" + escapeJson(s.name()) + "\"}";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
