package com.example.StudentsApplication.serverTEST;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RealStudentsValidationIT {

    private final String baseUrl = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void post_201_whenValid() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {"name":"Ana","age":20,"correo":"anana27@uni.es"}
                """, StandardCharsets.UTF_8))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, res.statusCode());
    }

    @Test
    void post_400_whenBodyEmpty() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
    }

    @Test
    void post_400_whenMalformedJson() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{name:\"Ana\"}"))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
    }

    @Test
    void post_400_whenNameBlank() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {"name":"   ","age":20,"correo":"ana@uni.es"}
                """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
    }

    @Test
    void post_400_whenAgeNegative() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {"name":"Ana","age":-1,"correo":"ana@uni.es"}
                """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
    }
}