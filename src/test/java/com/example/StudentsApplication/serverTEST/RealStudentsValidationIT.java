package com.example.StudentsApplication.serverTEST;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/*
 *
 * Clase de comprobacion de excepciones en el servidor real
 *
 */

class RealStudentsValidationIT {

    private final String baseUrl = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void post_201_whenValid() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                {"name":"Ana","age":20,"correo":"anana2@uni.es"}
            """, StandardCharsets.UTF_8))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, res.statusCode());
        assertTrue(res.body().contains("\"name\":\"Ana\""));
        assertTrue(res.body().contains("\"correo\":\"anana2@uni.es\""));
    }

    @Test
    void post_400_whenNameBlank() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                {"age":20,"correo":"anana2@uni.es"}
            """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("\"name\""));
    }

    @Test
    void post_400_whenEmailInvalid() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                {"name":"Ana","age":20,"correo":"correo-invalido"}
            """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("\"correo\""));
    }

    @Test
    void post_400_whenAgeLessThan1() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                {"name":"Ana","age":0,"correo":"anana2@uni.es"}
            """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("\"age\""));
    }
}