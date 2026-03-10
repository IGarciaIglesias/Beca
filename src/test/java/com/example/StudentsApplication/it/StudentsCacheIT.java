package com.example.StudentsApplication.it;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests de integración EXTERNOS contra el servidor YA ARRANCADO.
 *
 * Opción 2: los endpoints /students/cache/... existen sólo si activas la propiedad:
 *   students.cache.debug.enabled=true
 *
 * Arranque recomendado del servidor:
 *   - IntelliJ (Program arguments):
 *       --students.cache.debug.enabled=true
 *     (o VM options: -Dstudents.cache.debug.enabled=true)
 *
 *   - Maven CLI:
 *       mvn spring-boot:run -Dspring-boot.run.arguments="--students.cache.debug.enabled=true"
 *
 * Si los endpoints de depuración no están activos, los tests se MARCAN COMO OMITIDOS
 * (no fallan) con un mensaje indicativo.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StudentsCacheIT {

    private static Long studentId;
    private static String studentEmail;
    private static boolean debugAvailable = false;

    @BeforeAll
    static void setup() {
        // URL base (permite override por propiedad o variable de entorno)
        RestAssured.baseURI = System.getProperty(
                "STUDENTS_BASE_URL",
                System.getenv().getOrDefault("STUDENTS_BASE_URL", "http://localhost:8080")
        );

        // Intentamos limpiar caché si existiera el endpoint
        try { get("/students/refresh"); } catch (Exception ignored) {}

        // Verificamos disponibilidad de endpoints de depuración
        try {
            int status = get("/students/cache/stats").then().extract().statusCode();
            debugAvailable = (status == 200);
        } catch (Exception ex) {
            debugAvailable = false;
        }

        // Generamos un correo único para no chocar con la restricción de email
        long rnd = ThreadLocalRandom.current().nextLong(1_000_000_000L);
        studentEmail = "ana." + rnd + "@example.com";

        if (!debugAvailable) {
            System.err.println(
                    "\n[AVISO] Endpoints de depuración de caché NO activos (404). " +
                            "Arranca el servidor con:\n" +
                            "  --students.cache.debug.enabled=true  (o -Dstudents.cache.debug.enabled=true)\n" +
                            "Ejemplo Maven:\n" +
                            "  mvn spring-boot:run -Dspring-boot.run.arguments=\"--students.cache.debug.enabled=true\"\n"
            );
        }
    }

    @Test @Order(1)
    @DisplayName("POST crea estudiante y la 1ª lectura por id lo deja cacheado (contains=true)")
    void create_and_prime_cache_by_id() {
        assumeTrue(debugAvailable,
                "Debug de caché no activo: arranca con --students.cache.debug.enabled=true");

        // Crear estudiante
        String payload = """
        {"name":"Ana","age":22,"correo":"%s"}
        """.formatted(studentEmail);

        Response r = given()
                .contentType("application/json")
                .body(payload)
                .post("/students")
                .then()
                .statusCode(201)
                .extract().response();

        JsonPath jp = r.jsonPath();
        studentId = jp.getLong("id");
        assertThat("Debe devolver id tras crear", studentId, notNullValue());

        // 1ª lectura por id -> cache-aside (carga y guarda en caché)
        get("/students/{id}", studentId).then().statusCode(200);

        // Debe estar presente en caché
        boolean present = get("/students/cache/contains/{id}", studentId)
                .then().statusCode(200)
                .extract().jsonPath().getBoolean("present");
        assertThat("El id debería estar en caché tras 1ª lectura", present, is(true));
    }

    @Test @Order(2)
    @DisplayName("PUT actualiza entidad y mantiene el id presente en caché")
    void put_updates_value_and_keeps_id_cached() {
        assumeTrue(debugAvailable,
                "Debug de caché no activo: arranca con --students.cache.debug.enabled=true");

        String payload = """
        {"name":"Ana G.","age":23,"correo":"%s"}
        """.formatted(studentEmail);

        given().contentType("application/json")
                .body(payload)
                .put("/students/{id}", studentId)
                .then()
                .statusCode(anyOf(is(200), is(202)));

        // GET por id refleja cambio
        String newName = get("/students/{id}", studentId)
                .then().statusCode(200)
                .extract().jsonPath().getString("name");
        assertThat(newName, equalTo("Ana G."));

        // El id sigue cacheado
        boolean present = get("/students/cache/contains/{id}", studentId)
                .then().statusCode(200)
                .extract().jsonPath().getBoolean("present");
        assertThat("Tras PUT, el id debe seguir en caché", present, is(true));
    }

    @Test @Order(3)
    @DisplayName("Listado cacheado ('all'): POST invalida la lista y al listar se repuebla")
    void list_cached_then_post_invalidates_list() {
        assumeTrue(debugAvailable,
                "Debug de caché no activo: arranca con --students.cache.debug.enabled=true");

        // 1ª lista -> se cachea 'all'
        get("/students").then().statusCode(200);
        boolean listPresent = get("/students/cache/stats")
                .then().statusCode(200)
                .extract().jsonPath().getBoolean("listAllPresent");
        assertThat("Después de listar, 'all' debe existir en la caché de listas", listPresent, is(true));

        // POST nuevo -> invalida 'all'
        String payload = """
        {"name":"Luis","age":23,"correo":"luis.%d@example.com"}
        """.formatted(ThreadLocalRandom.current().nextLong(1_000_000L));

        given().contentType("application/json")
                .body(payload)
                .post("/students")
                .then().statusCode(201);

        boolean listAfterPost = get("/students/cache/stats")
                .then().statusCode(200)
                .extract().jsonPath().getBoolean("listAllPresent");
        assertThat("Tras POST, 'all' debe haberse invalidado", listAfterPost, is(false));

        // Se repuebla al volver a listar
        get("/students").then().statusCode(200);
        boolean listPresentAgain = get("/students/cache/stats")
                .then().statusCode(200)
                .extract().jsonPath().getBoolean("listAllPresent");
        assertThat("Después de listar de nuevo, 'all' debe repoblarse", listPresentAgain, is(true));
    }

    @Test @Order(4)
    @DisplayName("Refresh limpia ambos mapas de caché")
    void refresh_clears_both_maps() {
        assumeTrue(debugAvailable,
                "Debug de caché no activo: arranca con --students.cache.debug.enabled=true");

        get("/students/refresh").then().statusCode(anyOf(is(200), is(204)));

        JsonPath stats = get("/students/cache/stats").then().statusCode(200).extract().jsonPath();
        int byId = stats.getInt("studentsSize");
        int lists = stats.getInt("studentsListSize");
        boolean listPresent = stats.getBoolean("listAllPresent");

        assertThat("Mapa por id vacío", byId, equalTo(0));
        assertThat("Mapa de listas vacío", lists, equalTo(0));
        assertThat("'all' no presente", listPresent, is(false));
    }

    @Test @Order(5)
    @DisplayName("Tras refresh, 1ª lectura por id repuebla solo ese id (lista 'all' aún ausente)")
    void get_by_id_after_refresh_repopulates_only_that_id() {
        assumeTrue(debugAvailable,
                "Debug de caché no activo: arranca con --students.cache.debug.enabled=true");

        get("/students/{id}", studentId).then().statusCode(200);

        JsonPath stats = get("/students/cache/stats").then().statusCode(200).extract().jsonPath();
        int byId = stats.getInt("studentsSize");
        boolean listPresent = stats.getBoolean("listAllPresent");

        assertThat("Debe haber al menos un id cacheado", byId, greaterThanOrEqualTo(1));
        assertThat("Listado 'all' no se repone hasta llamar a /students", listPresent, is(false));
    }

    @Test @Order(6)
    @DisplayName("DELETE (soft por defecto) evicta el id y deja de existir")
    void delete_evicts_id() {
        assumeTrue(debugAvailable,
                "Debug de caché no activo: arranca con --students.cache.debug.enabled=true");

        delete("/students/{id}", studentId)
                .then()
                .statusCode(anyOf(is(204), is(200)));

        // Ya no existe
        get("/students/{id}", studentId)
                .then().statusCode(404);

        // Y no está en caché
        boolean present = get("/students/cache/contains/{id}", studentId)
                .then().statusCode(200)
                .extract().jsonPath().getBoolean("present");
        assertThat("Tras DELETE, el id no debe estar en caché", present, is(false));
    }
}