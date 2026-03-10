package com.example.StudentsApplication.external;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Simula el flujo CRUD + soft delete + restore + hard delete
 * sin depender del backend real.
 */
class WireMockStudentsCrudIT extends WireMockSupport {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void crud_soft_restore_hard_mocked() throws Exception {
        // CREATE -> 201 {id:10,...}
        wm.stubFor(post(urlEqualTo("/students"))
                .willReturn(aResponse().withStatus(201).withHeader("Content-Type","application/json")
                        .withBody("""
                                {"id":10,"name":"Ana","age":20,"correo":"ana@uni.es","deleted":false}""")));

        // LIST (inicial) -> contiene id 10
        wm.stubFor(get(urlEqualTo("/students"))
                .inScenario("soft-restore-hard")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type","application/json")
                        .withBody("""
                                [{"id":10,"name":"Ana","age":20,"correo":"ana@uni.es","deleted":false}]""")));

        // GET /10 -> 200
        wm.stubFor(get(urlEqualTo("/students/10"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("""
                                {"id":10,"name":"Ana","age":20,"correo":"ana@uni.es","deleted":false}""")));

        // PUT /10 -> 200 (actualiza)
        wm.stubFor(put(urlEqualTo("/students/10"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("""
                                {"id":10,"name":"Ana Gomez","age":21,"correo":"ana.gomez@uni.es","deleted":false}""")));

        // PATCH /10 -> 200 (age=22)
        wm.stubFor(patch(urlEqualTo("/students/10"))
                .withRequestBody(matchingJsonPath("$.age", matching("22")))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("""
                                {"id":10,"name":"Ana Gomez","age":22,"correo":"ana.gomez@uni.es","deleted":false}""")));

        // DELETE /10 (soft) -> 204 y pasamos a estado "soft-deleted"
        wm.stubFor(delete(urlEqualTo("/students/10"))
                .inScenario("soft-restore-hard").whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(204))
                .willSetStateTo("soft-deleted"));

        // LIST tras soft -> vacío (¡sin cambiar de estado!)
        wm.stubFor(get(urlEqualTo("/students"))
                .inScenario("soft-restore-hard").whenScenarioStateIs("soft-deleted")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type","application/json")
                        .withBody("[]")));
        // Importante: NO .willSetStateTo("restoring") aquí

        // GET /10 tras soft -> 404 (seguimos en "soft-deleted")
        wm.stubFor(get(urlEqualTo("/students/10"))
                .inScenario("soft-restore-hard").whenScenarioStateIs("soft-deleted")
                .willReturn(aResponse().withStatus(404)));

        // RESTORE (PATCH {"deleted":false}) -> 200 y estado "restored"
        wm.stubFor(patch(urlEqualTo("/students/10"))
                .withRequestBody(matchingJsonPath("$.deleted", matching("false")))
                .inScenario("soft-restore-hard").whenScenarioStateIs("soft-deleted")
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("""
                                {"id":10,"name":"Ana Gomez","age":22,"correo":"ana.gomez@uni.es","deleted":false}"""))
                .willSetStateTo("restored"));

        // LIST tras restore -> vuelve a aparecer
        wm.stubFor(get(urlEqualTo("/students"))
                .inScenario("soft-restore-hard").whenScenarioStateIs("restored")
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("""
                                [{"id":10,"name":"Ana Gomez","age":22,"correo":"ana.gomez@uni.es","deleted":false}]""")));

        // DELETE hard -> 204 y estado "hard-deleted"
        wm.stubFor(delete(urlEqualTo("/students/10?soft=false"))
                .inScenario("soft-restore-hard").whenScenarioStateIs("restored")
                .willReturn(aResponse().withStatus(204))
                .willSetStateTo("hard-deleted"));

        // LIST final -> vacío
        wm.stubFor(get(urlEqualTo("/students"))
                .inScenario("soft-restore-hard").whenScenarioStateIs("hard-deleted")
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json")
                        .withBody("[]")));

        // —— Llamadas reales (HTTP) al mock:

        // POST
        var resPost = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .header("Content-Type","application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                """
                                        {"name":"Ana","age":20,"correo":"ana@uni.es"}""", StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resPost.statusCode());

        // LIST (inicial)
        var resList1 = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(resList1.body().contains("\"id\":10"));

        // PUT
        var resPut = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students/10"))
                        .header("Content-Type","application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(
                                """
                                        {"name":"Ana Gomez","age21,"correo":"ana.gomez@uni.es"}""", StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resPut.statusCode());

        // PATCH (age=22)
        var resPatch = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students/10"))
                        .header("Content-Type","application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                                {"age":22}""", StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resPatch.statusCode());

        // DELETE soft
        var resDelSoft = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students/10"))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(resDelSoft.statusCode()==204 || resDelSoft.statusCode()==200);

        // LIST tras soft -> vacío (seguimos en "soft-deleted")
        var resListSoft = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resListSoft.statusCode());
        assertFalse(resListSoft.body().contains("\"id\":10"));

        // GET tras soft -> 404 (seguimos en "soft-deleted")
        var resGetSoft = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students/10"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resGetSoft.statusCode());

        // RESTORE
        var resRestore = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students/10"))
                        .header("Content-Type","application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                                {"deleted":false}""", StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resRestore.statusCode());

        // LIST tras restore
        var resList2 = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(resList2.body().contains("\"id\":10"));

        // DELETE hard
        var resDelHard = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students/10?soft=false"))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(resDelHard.statusCode()==204 || resDelHard.statusCode()==200);

        // LIST final
        var resList3 = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/students"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertFalse(resList3.body().contains("\"id\":10"));
    }
}
