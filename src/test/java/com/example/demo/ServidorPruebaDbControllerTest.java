package com.example.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServidorPruebaDbControllerTest {

    String urlBase = "http://localhost:8080/";
    public static HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> peticionGet(String urlCompleta) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlCompleta))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Test realizado -> " + response);
        return response;
    }

    HttpResponse<String> peticionPost(String urlCompleta, String jsonData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlCompleta))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("Test realizado -> " + response);
        return response;
    }

    HttpResponse<String> peticionPut(String urlCompleta, int id, String jsonData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlCompleta + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonData))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("Test realizado -> " + response);
        return response;
    }

    HttpResponse<String> peticionPutActualizar(String urlCompleta, int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlCompleta + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("Test realizado -> " + response);
        return response;
    }

    HttpResponse<String> peticionPatch(String urlCompleta, int id, String jsonData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlCompleta + id))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("Test realizado -> " + response);
        return response;
    }

    HttpResponse<String> peticionDelete(String urlCompleta, int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlCompleta + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("Test realizado -> " + response);
        return response;
    }

    @Test
    void obtenerTodos() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes";
        int codigoRespuesta = peticionGet(urlCompleta).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    // Revisar en ambos que también sean válidos cuando el código de respuesta es 204
    @Test
    void obtenerNoEliminados() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/noEliminados";
        int codigoRespuesta = peticionGet(urlCompleta).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void obtenerEliminados() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/eliminados";
        int codigoRespuesta = peticionGet(urlCompleta).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void crarEstudiante() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/crear";
        String jsonData = "{\"nombre\": \"Juan\", \"edad\":40,\"correo\": \"juan@gmail.com\", \"deleted\": \"false\"}";
        int codigoRespuesta = peticionPost(urlCompleta, jsonData).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void actualizarEstudianteCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/completo/";
        String jsonData = "{\"nombre\": \"Juan\", \"edad\":40,\"correo\": \"juan@gmail.com\", \"deleted\": \"false\"}";
        int id = 1;
        int codigoRespuesta = peticionPut(urlCompleta, id, jsonData).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void actualizarEstudianteNoCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/completo/";
        String jsonData = "{\"nombre\": \"Juan\", \"edad\":40,\"correo\": \"juan@gmail.com\", \"deleted\": \"false\"}";
        int id = 9999999;
        int codigoRespuesta = peticionPut(urlCompleta, id, jsonData).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void actualizarEstudianteParcialmenteCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/parcial/";
        String jsonData = "{\"nombre\": \"\", \"edad\":40,\"correo\": \"\", \"deleted\": \"false\"}";
        int id = 1;
        int codigoRespuesta = peticionPatch(urlCompleta, id, jsonData).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void actualizarEstudianteParcialmenteNoCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/parcial/";
        String jsonData = "{\"nombre\": \"\", \"edad\":40,\"correo\": \"\", \"deleted\": \"false\"}";
        int id = 9999999;
        int codigoRespuesta = peticionPatch(urlCompleta, id, jsonData).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void actualizarEstadoEliminarSoftCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/borradoSoft/";
        int id = 15;
        int codigoRespuesta = peticionPutActualizar(urlCompleta, id).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void actualizarEstadoEliminarSoftNoCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/borradoSoft/";
        int id = 9999999;
        int codigoRespuesta = peticionPutActualizar(urlCompleta, id).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void eliminarEstudianteHardCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/borradoSoft/";
        int id = 13;
        int codigoRespuesta = peticionDelete(urlCompleta, id).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void eliminarEstudianteHardNoCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/borradoHard/";
        int id = 9999999;
        int codigoRespuesta = peticionDelete(urlCompleta, id).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void deshacerEliminadoSoftCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/devolverEstado/";
        int id = 15;
        int codigoRespuesta = peticionPutActualizar(urlCompleta, id).statusCode();
        assertEquals(200, codigoRespuesta);
    }

    @Test
    void deshacerEliminadoSoftNoCorrecto() throws IOException, InterruptedException {
        String urlCompleta = urlBase + "api/estudiantes/devolverEstado/";
        int id = 9999999;
        int codigoRespuesta = peticionPutActualizar(urlCompleta, id).statusCode();
        assertEquals(200, codigoRespuesta);
    }
}