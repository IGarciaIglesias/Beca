package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.*;


@ExtendWith(MockitoExtension.class)
class EstudianteServiceTests {

    Object[] datos = {};

    @Mock
    private EstudianteRepository repository;

    @InjectMocks
    private EstudianteService service;

    boolean validarNombre(String nombre){
        boolean nombreValido = nombre != null && !nombre.isEmpty() && nombre.matches("[a-zA-Z]");
        return nombreValido;
    }

    @Test
    void nombreValido(){
        String nombre = "laura";
        assertTrue("Nombre válido", validarNombre(nombre));
    }

    @Test
    void nombreInvalido1(){
        String nombre = "l324ura";
        assertTrue("Nombre no válido", validarNombre(nombre));
    }

    @Test
    void nombreInvalido2(){
        String nombre = " ";
        assertTrue("Nombre no válido", validarNombre(nombre));
    }

    @Test
    void nombreInvalido3(){
        String nombre = "";
        assertTrue("Nombre no válido", validarNombre(nombre));
    }

    boolean validarEdad(int edad){
        boolean edadValida = edad >= 0 && edad <= 120 && edad <= -1;
        return  edadValida;
    }

    @Test
    void edadValida(){
        int edad = 34;
        assertTrue("Edad válida", validarEdad(edad));
    }

    @Test
    void edadInvalido1(){
        int edad = -35;
        assertTrue("Edad no válida", validarEdad(edad));
    }

    @Test
    void edadInvalido2(){
        int edad = 0;
        assertTrue("Edad no válida", validarEdad(edad));
    }

    @Test
    void edadInvalido3(){
        int edad = 150;
        assertTrue("Edad no válida", validarEdad(edad));
    }

    boolean validarCorreo(String correo){
        boolean correoValido = correo.matches(("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"));
        return correoValido;
    }

    @Test
    void correoValido(){
        String correo = "laura@gmail.com";
        assertTrue("Correo válido", validarCorreo(correo));
    }

    @Test
    void correoInvalido1(){
        String correo = "lauragmail.com";
        assertTrue("Correo no válido", validarCorreo(correo));
    }

    @Test
    void correoInvalido2(){
        String correo = "laura@gmailcom";
        assertTrue("Correo no válido", validarCorreo(correo));
    }

    @Test
    public void testMock(){
        assertNotNull("existe",repository);
    }

    // Función para validar los datos antes de hacer la operación del crud pertinente
    boolean validarDatos(Object[] datos){
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();

        Boolean nombreValido = validarNombre(nombre);
        Boolean edadValida = validarEdad(edad);
        Boolean correoValido = validarCorreo(correo);
        System.out.println("nombre válido: " + nombreValido);
        System.out.println("edad válida: " + edadValida);
        System.out.println("correo válido: " + correoValido);

        if (nombreValido && edadValida && correoValido){
            System.out.println("DATOS válidos");
            return true;
        } else {
            return false;
        }
    }

    // CRUD
    @Test
    void crearUsuarioValido() {
        datos = new Object[]{1L, "Juan", 44, "juan@example.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos){
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);

            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            Estudiante guardarUsuario = repository.save(estudiante);

            assertThat(guardarUsuario.getId()).isNotNull();
            assertThat(guardarUsuario.getNombre()).isEqualTo("Juan");
            assertThat(guardarUsuario.getCorreo()).isEqualTo("juan@example.com");
        }
    }

    @Test
    void crearUsuarioNombreNoValido() {
        datos = new Object[]{1L, "Ju3n", 44, "juan@example.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos){
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);

            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            Estudiante guardarUsuario = repository.save(estudiante);

            assertThat(guardarUsuario.getId()).isNotNull();
            assertThat(guardarUsuario.getNombre()).isEqualTo("Ju4n");
            assertThat(guardarUsuario.getCorreo()).isEqualTo("juan@example.com");
        }
    }

    @Test
    void crearUsuarioEdadNoValida() {
        datos = new Object[]{1L, "Juan", -44, "juan@example.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos){
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);

            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            Estudiante guardarUsuario = repository.save(estudiante);

            assertThat(guardarUsuario.getId()).isNotNull();
            assertThat(guardarUsuario.getNombre()).isEqualTo("Juan");
            assertThat(guardarUsuario.getEdad()).isEqualTo(44);
            assertThat(guardarUsuario.getCorreo()).isEqualTo("juan@example.com");
        }
    }

    @Test
    void crearUsuarioCorreoNoValido() {
        datos = new Object[]{1L, "Juan", 44, "juanexample.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos){
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);

            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            Estudiante guardarUsuario = repository.save(estudiante);

            assertThat(guardarUsuario.getId()).isNotNull();
            assertThat(guardarUsuario.getNombre()).isEqualTo("Juan");
            assertThat(guardarUsuario.getEdad()).isEqualTo(44);
            assertThat(guardarUsuario.getCorreo()).isEqualTo("juanexample.com");
        }
    }

    @Test
    void leerEstudiante() {
        datos = new Object[]{1L, "Juan", 44, "juan@example.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos) {
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);


            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            when(repository.findById(2L)).thenReturn(Optional.of(estudiante));
            repository.save(estudiante);

            Optional<Estudiante> estudianteEncontrado = repository.findById(2L);

            assertThat(estudianteEncontrado).isPresent();
            assertThat(estudianteEncontrado.get().getCorreo()).isEqualTo("juan@examplee.com");
        }
    }

    @Test
    void leerEstudianteNombreNoValido() {
        datos = new Object[]{1L, "Ju3n", 44, "juan@example.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos) {
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);


            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            when(repository.findById(2L)).thenReturn(Optional.of(estudiante));
            repository.save(estudiante);

            Optional<Estudiante> estudianteEncontrado = repository.findById(2L);

            assertThat(estudianteEncontrado).isPresent();
            assertThat(estudianteEncontrado.get().getCorreo()).isEqualTo("juan@example.com");
        }
    }

    @Test
    void leerEstudianteEdadNoValida() {
        datos = new Object[]{1L, "Juan", -44, "juan@example.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos) {
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);


            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            when(repository.findById(2L)).thenReturn(Optional.of(estudiante));
            repository.save(estudiante);

            Optional<Estudiante> estudianteEncontrado = repository.findById(2L);

            assertThat(estudianteEncontrado).isPresent();
            assertThat(estudianteEncontrado.get().getCorreo()).isEqualTo("juan@example.com");
        }
    }

    @Test
    void leerEstudianteCorreoNoValido() {
        datos = new Object[]{1L, "Juan", 44, "juanexample.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos) {
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);


            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            when(repository.findById(2L)).thenReturn(Optional.of(estudiante));
            repository.save(estudiante);

            Optional<Estudiante> estudianteEncontrado = repository.findById(2L);

            assertThat(estudianteEncontrado).isPresent();
            assertThat(estudianteEncontrado.get().getCorreo()).isEqualTo("juanexample.com");
        }
    }

    @Test
    void actualizarEstudiante() {
        datos = new Object[]{1L, "Juan", 44, "juan@example.com", false};
        Long id = (Long) datos[0];
        String nombre = datos[1].toString();
        int edad = (int) datos[2];
        String correo = datos[3].toString();
        Boolean eliminado = (Boolean) datos[4];

        boolean datosValidos = validarDatos(datos);

        if (datosValidos) {
            Estudiante estudiante = new Estudiante(id, nombre, edad, correo, eliminado);

            when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
            Estudiante guardarEstudiante = repository.save(estudiante);

            guardarEstudiante.setNombre("Carlos R.");
            Estudiante updatedUser = repository.save(guardarEstudiante);

            assertThat(updatedUser.getNombre()).isEqualTo("Carlos R.");
        }
    }

    @Test
    void eliminarEstudiante() {
        Estudiante estudiante = new Estudiante(4L, "Laura Gómez", 19, "laura@example.com", false);

        when(repository.save(any(Estudiante.class))).thenReturn(estudiante);
        Estudiante guardarEstudiante = repository.save(estudiante);

        repository.deleteById(guardarEstudiante.getId());

        Optional<Estudiante> deletedUser = repository.findById(guardarEstudiante.getId());
        assertThat(deletedUser).isEmpty();
    }
}