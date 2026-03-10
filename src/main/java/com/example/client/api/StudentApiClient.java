package com.example.client.api;

import com.example.client.model.Student;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class StudentApiClient {

    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public StudentApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    // ---------- CRUD ----------

    public List<Student> list() throws IOException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students"))
                .GET()
                .header("Accept", "application/json")
                .build();
        String body = send(req);
        try {
            return mapper.readValue(body, new TypeReference<List<Student>>() {});
        } catch (IOException e) {
            throw new IOException("Error parseando lista de estudiantes: " + body, e);
        }
    }

    public Student get(long id) throws IOException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students/" + id))
                .GET()
                .header("Accept", "application/json")
                .build();
        String body = send(req);
        try {
            return mapper.readValue(body, Student.class);
        } catch (IOException e) {
            throw new IOException("Error parseando estudiante: " + body, e);
        }
    }

    public Student create(Student s) throws IOException {
        String json = mapper.writeValueAsString(s);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        String body = send(req);
        try {
            return mapper.readValue(body, Student.class);
        } catch (IOException e) {
            throw new IOException("Error parseando respuesta de creación: " + body, e);
        }
    }

    public Student update(long id, Student s) throws IOException {
        String json = mapper.writeValueAsString(s);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students/" + id))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        String body = send(req);
        try {
            return mapper.readValue(body, Student.class);
        } catch (IOException e) {
            throw new IOException("Error parseando respuesta de update: " + body, e);
        }
    }

    /** PATCH parcial */
    public Student patch(long id, Map<String, Object> fields) throws IOException {
        String json = mapper.writeValueAsString(fields);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students/" + id))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                // HttpClient no tiene método PATCH directo; se usa .method("PATCH", ...)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        String body = send(req);
        try {
            return mapper.readValue(body, Student.class);
        } catch (IOException e) {
            throw new IOException("Error parseando respuesta de patch: " + body, e);
        }
    }

    /** Soft delete por defecto */
    public void delete(long id) throws IOException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students/" + id))
                .header("Accept", "application/json")
                .DELETE()
                .build();
        send(req); // 204 No Content
    }

    /** Hard delete: ?soft=false */
    public void deleteHard(long id) throws IOException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students/" + id + "?soft=false"))
                .header("Accept", "application/json")
                .DELETE()
                .build();
        send(req); // 204 No Content
    }

    /** Restaurar: PATCH deleted=false */
    public Student restore(long id) throws IOException {
        String json = mapper.writeValueAsString(Map.of("deleted", false));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students/" + id))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        String body = send(req);
        try {
            return mapper.readValue(body, Student.class);
        } catch (IOException e) {
            throw new IOException("Error parseando respuesta de restore: " + body, e);
        }
    }

    // ---------- util ----------

    private String send(HttpRequest req) throws IOException {
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = res.statusCode();
            String body = res.body() == null ? "" : res.body();
            if (code >= 400) {
                throw new IOException("HTTP " + code + (body.isBlank() ? "" : (": " + body)));
            }
            return body;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        }
    }
}