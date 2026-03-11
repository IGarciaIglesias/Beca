package com.example.StudentsApplication.serverTEST;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RealStudentsCrudIT {

    private final String baseUrl = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void crud_soft_restore_hard_real() throws Exception {

        // CREATE
        var post = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("""
                            {"name":"Ana","age":20,"correo":"crud@uni.es"}
                        """, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, post.statusCode());
        long id = extractId(post.body());

        // GET by id
        var get = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, get.statusCode());

        // PUT
        var put = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                            {"name":"Ana Mod","age":21,"correo":"mod@uni.es"}
                        """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, put.statusCode());

        // PATCH
        var patch = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                            {"age":22}
                        """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, patch.statusCode());

        // DELETE soft
        var delSoft = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(204, delSoft.statusCode());

        // GET after soft → 404
        var getSoft = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, getSoft.statusCode());

        // RESTORE
        var restore = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                            {"deleted":false}
                        """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, restore.statusCode());

        // DELETE hard
        var delHard = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id + "?soft=false"))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(204, delHard.statusCode());
    }

    @Test
    void operations_on_non_existing_id_return_404() throws Exception {

        long fakeId = 999999;

        assertEquals(404,
                client.send(
                        HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + fakeId)).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                ).statusCode()
        );

        // PUT a id inexistente -> cualquier 4xx (evita 415 añadiendo Content-Type)
        var putRes = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + fakeId))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                    {"name":"X","age":20,"correo":"x@uni.es"}
                """, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(putRes.statusCode() >= 400 && putRes.statusCode() < 500,
                "Se esperaba 4xx en PUT a id inexistente, pero fue " + putRes.statusCode()
                        + " body=" + putRes.body());


        // PATCH a id inexistente -> cualquier 4xx (evita 415 añadiendo Content-Type)
        var patchRes = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + fakeId))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                    {"age":22}
                """, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(patchRes.statusCode() >= 400 && patchRes.statusCode() < 500,
                "Se esperaba 4xx en PATCH a id inexistente, pero fue " + patchRes.statusCode()
                        + " body=" + patchRes.body());

        assertEquals(404,
                client.send(
                        HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + fakeId + "?soft=false"))
                                .DELETE().build(),
                        HttpResponse.BodyHandlers.ofString()
                ).statusCode()
        );
    }

    private long extractId(String json) {
        int p = json.indexOf("\"id\":");
        return Long.parseLong(json.substring(p + 5).split("[,}]")[0].trim());
    }
}