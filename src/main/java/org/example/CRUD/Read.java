package org.example.CRUD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.example.Main.*;

public class Read {

    private static String pasarAJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Main.Estudiante> estudiantes = mapper.readValue(json, new TypeReference<>() {});

        estudiantes.forEach(System.out::println);
        return json;
    }

    // Función para mostrar una lista de todos los usuarios registrados en la base de datos
    public static String mostrarTodosLosUsuario() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/estudiantes"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 200){
            String json = response.body();
            pasarAJson(json);
            return json;
        } else {
            return null;
        }
    }

    // Función para mostrar una lista de todos los usuarios registrados, pero en este caso
    // solo se mostrarán los que están marcados como eliminados mediante el método de soft delete
    public static String mostrarUsuariosBorrados() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/estudiantes/eliminados"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 200){
            String json = response.body();
            pasarAJson(json);
            return json;
        } else {
            return null;
        }
    }

    // Función para mostrar una lista de todos los usuarios registrados, pero en este caso
    // solo se mostrarán los que están marcados como eliminados mediante el método de soft delete
    public static String mostrarUsuariosNoBorrados() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/estudiantes/noEliminados"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 200){
            String json = response.body();
            pasarAJson(json);
            return json;
        } else {
            return null;
        }
    }
}