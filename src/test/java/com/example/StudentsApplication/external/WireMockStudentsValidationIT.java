package com.example.StudentsApplication.external;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Tests de validación del endpoint POST /students usando WireMock.
 * No requiere que el backend real esté levantado.
 */
class WireMockStudentsValidationIT extends WireMockSupport {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void post_201_whenValid() throws Exception {
        // Stub para creación válida
        JsonNode created = mapper.readTree("""
          {"id":1,"name":"Ana","age":20,"correo":"ana@uni.es","deleted":false}
        """);
        wm.stubFor(post(urlEqualTo("/students"))
                .withRequestBody(matchingJsonPath("$.name", matching("Ana")))
                .willReturn(aResponse().withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(created.toString())));

        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                  {"name":"Ana","age":20,"correo":"ana@uni.es"}
                """, StandardCharsets.UTF_8))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, res.statusCode(), res.body());
        assertTrue(res.body().contains("\"name\":\"Ana\""));
        assertTrue(res.body().contains("\"correo\":\"ana@uni.es\""));
    }

    @Test
    void post_400_whenNameBlank() throws Exception {
        // Stub para 400 cuando falta/va vacío 'name'
        wm.stubFor(post(urlEqualTo("/students"))
                .withRequestBody(matchingJsonPath("$.name", absent()))
                .willReturn(aResponse().withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                  {"status":400,"errors":{"name":"El nombre no puede estar vacío"}}""")));

        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                  {"age":20,"correo":"ana@uni.es"}
                """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode(), res.body());
        assertTrue(res.body().contains("\"name\""));
    }

    @Test
    void post_400_whenEmailInvalid() throws Exception {
        wm.stubFor(post(urlEqualTo("/students"))
                .withRequestBody(matchingJsonPath("$.correo", matching("correo-invalido")))
                .willReturn(aResponse().withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"status":400,"errors":{"correo":"Formato de correo inválido"}}""")));

        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                  {"name":"Ana","age":20,"correo":"correo-invalido"}
                """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode(), res.body());
        assertTrue(res.body().contains("\"correo\""));
    }

    @Test
    void post_400_whenAgeLessThan1() throws Exception {
        wm.stubFor(post(urlEqualTo("/students"))
                .withRequestBody(matchingJsonPath("$.age", matching("0")))
                .willReturn(aResponse().withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"status":400,"errors":{"age":"La edad debe ser >= 1"}}""")));

        var req = HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                  {"name":"Ana","age":0,"correo":"ana@uni.es"}
                """))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode(), res.body());
        assertTrue(res.body().contains("\"age\""));
    }
}