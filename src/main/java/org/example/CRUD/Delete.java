package org.example.CRUD;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.example.CRUD.Read.mostrarTodosLosUsuario;
import static org.example.CRUD.Read.mostrarUsuariosNoBorrados;

import static org.example.Main.*;

public class Delete {
    // Función para borrar un usuario por su id, se muestra igual que en el caso anterior todos los usuarios
    // inicialmente, y se selcciona uno para borrarlo
    // TODO: confirmación de borrado -- HECHO
    public static void borrarUsuarioPorId() throws IOException, InterruptedException {
        mostrarTodosLosUsuario();

        scanner.nextLine();
        System.out.print("Ingrese el id del estudiante que desee borrar ");
        Long id = scanner.nextLong();
        scanner.nextLine();
        System.out.println("¿Está seguro de borrar este usuario? S/N");
        String op = scanner.nextLine();
        String normalizarOp = op.toLowerCase();

        if (normalizarOp.equals("s")) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/estudiantes/borradoHard/" + id))
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            menu();
        } else if (normalizarOp.equals("n")) {
            menu();
        } else {
            System.out.println("Opción no válida");
            borrarUsuarioPorId();
        }
    }

    // Función para borrar un usuario por su id temporalmente, softdelete
    public static void borrarUsuarioPorIdTemporalmente() throws IOException, InterruptedException {
        mostrarUsuariosNoBorrados();

        System.out.print("Ingrese el id del estudiante que desee borrar " + scanner.nextLine());
        Long id = scanner.nextLong();
        scanner.nextLine();

        String op = scanner.nextLine();
        String normalizarOp = op.toLowerCase();

        if (normalizarOp.equals("s")) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/estudiantes/borradoSoft/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            menu();
        } else if (normalizarOp.equals("n")) {
            menu();
        } else {
            System.out.println("Opción no válida");
            borrarUsuarioPorId();
        }
    }
}
