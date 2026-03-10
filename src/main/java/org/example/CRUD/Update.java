package org.example.CRUD;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.example.CRUD.Read.mostrarUsuariosBorrados;
import static org.example.Main.*;

import static org.example.CRUD.Read.mostrarTodosLosUsuario;

public class Update {
    // Función para editar un usuario por su número de id, se mostrará una lista de todos los usuarios
    // para elegir uno de los existentes y editarlo
    public static void editarUsuarioPorId() throws IOException, InterruptedException {
        mostrarTodosLosUsuario();

        System.out.print("Ingrese el id del estudiante que desee editar " + scanner.nextLine());
        Long id = scanner.nextLong();
        scanner.nextLine();

        System.out.print("Ingrese el nuevo nombre del estudiante ");
        String nombre = scanner.nextLine();

        System.out.print("Ingrese la nueva edad del estudiante ");
        int edad = scanner.nextInt();

        scanner.nextLine();

        System.out.print("Ingrese el nuevo correo del estudiante ");
        String correo = scanner.nextLine();

        if(!nombre.isEmpty() && edad != 0 && !correo.isEmpty() && correo.matches(("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))) {
            String jsonData = "{\"nombre\": \"" + nombre + "\", \"edad\":" + edad + ",\"correo\": \"" + correo + "\", \"deleted\": \"false\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/estudiantes/completo/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            menu();
        }
    }

    // Misma función que la anterior, pero en este caso se puede editar solo un dato del usuario que se
    // haya seleccionado
    public static void editarUsuarioPorIdParcialmente() throws IOException, InterruptedException {
        mostrarTodosLosUsuario();

        System.out.print("Ingrese el id del estudiante que desee editar " + scanner.nextLine());
        Long id = scanner.nextLong();
        scanner.nextLine();

        System.out.println("Escriba su nuevo nombre (Dejar en blanco para mantener la información anterior) ");
        String nuevoNombre = scanner.nextLine();

        System.out.println("Escriba su nueva edad (0 para mantener la información anterior) ");
        int nuevaEdad = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Ingrese el nuevo correo del estudiante ");
        String nuevoCorreo = scanner.nextLine();

        if (nuevoNombre.isEmpty() || !nuevoNombre.isEmpty() && nuevaEdad == 0 || nuevaEdad != 0 && nuevoCorreo.isEmpty() || nuevoCorreo.matches(("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))) {
            String jsonData = "{\"nombre\": \"" + nuevoNombre + "\", \"edad\":" + nuevaEdad + ",\"correo\": \"" + nuevoCorreo + "\", \"deleted\": \"false\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/estudiantes/parcial/" + id))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            menu();
        }
    }

    // Función para recuperar los usuarios que han sido eliminados con softdelete
    public static void recuperarUsuariosTemporales() throws IOException, InterruptedException {
        mostrarUsuariosBorrados();
        System.out.println("Ingrese el id del usuario que desee regresar a su estado original (0 para salir) ");
        long id = scanner.nextLong();

        if (id > 0) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/estudiantes/devolverEstado/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            recuperarUsuariosTemporales();
        } else {
            menu();
        }
    }
}
