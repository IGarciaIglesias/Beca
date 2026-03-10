package org.example;

import org.example.CRUD.Read;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.example.CRUD.Create.crearNuevoUsuario;
import static org.example.CRUD.Delete.borrarUsuarioPorId;
import static org.example.CRUD.Delete.borrarUsuarioPorIdTemporalmente;
import static org.example.CRUD.Read.*;
import static org.example.CRUD.Update.*;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws IOException, InterruptedException {
        SwingUtilities.invokeLater(() -> {
            try {
                PanelDeGestion panelDeGestion = new PanelDeGestion();
                panelDeGestion.setVisible(true);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        //menu();
    }

    public static void menu() throws IOException, InterruptedException {
        System.out.println("----------------------------------");
        System.out.println("Menú de gestión");
        System.out.println("----------------------------------");
        System.out.println("1.- Ver todos los estudiantes");
        System.out.println("2.- Ver todos los estudiantes eliminados temporalmente");
        System.out.println("3.- Ver todos los estudiantes no eliminados temporalmente");
        System.out.println("4.- Crear un nuevo estudiante");
        System.out.println("5.- Editar un usuario existente (Al completo)");
        System.out.println("6.- Editar un usuario existente (Parcialmente)");
        System.out.println("7.- Borrar un usuario existente (Para siempre)");
        System.out.println("8.- Borrar un usuario existente (Temporal)");
        System.out.println("9.- Recuperar usuarios borrados temporalmente");
        System.out.println("10.- Cerrar programa");
        System.out.println("----------------------------------");
        int seleccion = scanner.nextInt();
        scanner.nextLine();

        switch (seleccion) {
            case 1:
                mostrarTodosLosUsuario();
                break;
            case 2:
                mostrarUsuariosBorrados();
                break;
            case 3:
                mostrarUsuariosNoBorrados();
                break;
            case 4:
                crearNuevoUsuario();
                break;
            case 5:
                editarUsuarioPorId();
                break;
            case 6:
                editarUsuarioPorIdParcialmente();
                break;
            case 7:
                borrarUsuarioPorId();
                break;
            case 8:
                borrarUsuarioPorIdTemporalmente();
                break;
            case 9:
                recuperarUsuariosTemporales();
                break;
            case 10:
                System.out.println("Cerrando programa de gestión...");
                break;
            default:
                System.out.println("Opción no válida");

        }
    }

    public static class Estudiante {
        public Long id;
        public String nombre;
        public int edad;
        public String correo;
        public boolean deleted;

        @Override
        public String toString() {
            return "Estudiante{" +
                    "id=" + id +
                    ", nombre='" + nombre + '\'' +
                    ", edad=" + edad +
                    ", correo='" + correo + '\'' +
                    ", deleted=" + deleted +
                    '}';
        }
    }
}