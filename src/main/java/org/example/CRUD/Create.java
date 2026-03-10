package org.example.CRUD;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.example.Main.*;

public class Create {

    // Función para crear un nuevo usuario, con validación de datos
    public static void crearNuevoUsuario() throws IOException, InterruptedException {
        System.out.print("Ingrese el nombre del nuevo estudiante ");
        String nombre = scanner.nextLine();

        System.out.print("Ingrese la edad del nuevo estudiante ");
        int edad = scanner.nextInt();

        scanner.nextLine();

        System.out.print("Ingrese el correo del nuevo estudiante ");
        String correo = scanner.nextLine();

        if(!nombre.isEmpty() && edad != 0 && !correo.isEmpty() && correo.matches(("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))) {
            String jsonData = "{\"nombre\": \"" + nombre + "\", \"edad\":" + edad + ",\"correo\": \"" + correo + "\", \"deleted\": \"false\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/estudiantes/crear"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            menu();
        } else {
            System.out.println("Datos no válidos");
            crearNuevoUsuario();
        }
    }
}
