package com.example.StudentsApplication.serverTEST;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/*
*
* Clase de TEST de crud al servidor REAL
*
*/

class RealStudentsCrudIT {

    private final String baseUrl = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void crud_soft_restore_hard_real() throws Exception {

        // CREATE -----------------------------------------
        var resPost = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("""
                    {"name":"Ana","age":20,"correo":"anaprueba2@uni.es"}
                """, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, resPost.statusCode());
        assertTrue(resPost.body().contains("\"id\""));

        long id = extractId(resPost.body());

        // LIST (1 student)
        var resList1 = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertTrue(resList1.body().contains("\"id\":" + id));

        // PUT ---------------------------------------------
        var resPut = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                    {"name":"Ana Gomez","age":21,"correo":"ana.gomez@uni.es"}
                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resPut.statusCode());

        // PATCH age=22 -------------------------------------
        var resPatchAge = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .header("Content-Type","application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                    {"age":22}
                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resPatchAge.statusCode());

        // DELETE SOFT ---------------------------------------
        var resDelSoft = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(204, resDelSoft.statusCode());

        // LIST after soft delete → must NOT contain student
        var resListSoft = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertFalse(resListSoft.body().contains("\"id\":" + id));

        // GET after soft delete → must return 404
        var resGetSoft = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, resGetSoft.statusCode());

        // RESTORE -----------------------------------------
        var resRestore = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id))
                        .header("Content-Type","application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                    {"deleted":false}
                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resRestore.statusCode());

        // LIST after restore → must contain student again
        var resList2 = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(resList2.body().contains("\"id\":" + id));

        // DELETE HARD --------------------------------------
        var resDelHard = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/" + id + "?soft=false"))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(204, resDelHard.statusCode());

        // LIST final → must be empty again
        var resListFinal = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertFalse(resListFinal.body().contains("\"id\":" + id));
    }

    private long extractId(String json) {
        int p = json.indexOf("\"id\":");
        return Long.parseLong(json.substring(p + 5).split("[,}]")[0].trim());
    }
}