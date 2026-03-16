package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServidorPruebaDbControllerTest {
    @Mock
    private EstudianteRepository repository;

    @Mock
    private EstudianteService service;

    @InjectMocks
    private ServidorPruebaDbController controller;

    @Test
    void cargarEstudiantes_204() {
        when(service.buscarEstudiante()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Estudiante>> resp = controller.obtenerTodos();

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void cargarEstudiantes_200() {
        when(service.buscarEstudiante()).thenReturn(List.of(new Estudiante()));

        ResponseEntity<List<Estudiante>> resp = controller.obtenerTodos();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isEmpty());
    }

    @Test
    void cargarEstudiantesBorrados_204() {
        when(service.buscarEstudiantesBorrados()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Estudiante>> resp = controller.obtenerEliminados();

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void cargarEstudiantesBorrados_200() {
        when(service.buscarEstudiantesBorrados()).thenReturn(List.of(new Estudiante()));

        ResponseEntity<List<Estudiante>> resp = controller.obtenerEliminados();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isEmpty());
    }

    @Test
    void cargarEstudiantesNoBorrados_204() {
        when(service.buscarEstudiantesNoBorrados()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Estudiante>> resp = controller.obtenerNoEliminados();

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void cargarEstudiantesNoBorrados_200() {
        when(service.buscarEstudiantesNoBorrados()).thenReturn(List.of(new Estudiante()));

        ResponseEntity<List<Estudiante>> resp = controller.obtenerNoEliminados();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isEmpty());
    }

    @Test
    void crarEstudifantes_200() {
        Estudiante estudiante = new Estudiante(1L, "María", 55, "maria@gmail.com", false);
        when(service.guardarEstudiante(estudiante)).thenReturn(estudiante);

        ResponseEntity<Estudiante> resp = controller.crarEstudiante(estudiante);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void actualizarEstudiante_204() {
        Long id = 99999L;

        // Estudiante actualizado
        Estudiante actualizado = new Estudiante();
        actualizado.setNombre("Nombre nuevo");
        actualizado.setEdad(20);
        actualizado.setCorreo("nuevo@gmail.com");
        actualizado.setDeleted(false);

        // Buscar estudiante, devuelve nada por que no existe
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Código de respuesta
        ResponseEntity<Estudiante> resp = controller.actualizarEstudiante(id, actualizado);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNull(resp.getBody());
    }

    @Test
    void actualizarEstudianteCompleto_200() {
        Long id = 1L;

        // Crear nuevo estudiante
        Estudiante existente = new Estudiante();
        existente.setNombre("Nombre viejo");
        existente.setEdad(18);
        existente.setCorreo("viejo@gmail.com");
        existente.setDeleted(false);

        // Actualizar los datos del estudiante creado
        Estudiante actualizado = new Estudiante();
        actualizado.setNombre("Nombre nuevo");
        actualizado.setEdad(20);
        actualizado.setCorreo("nuevo@gmail.com");
        existente.setDeleted(false);

        when(repository.findById(id)).thenReturn(Optional.of(existente));

        when(service.guardarEstudiante(any(Estudiante.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Código de respuesta
        ResponseEntity<Estudiante> resp = controller.actualizarEstudiante(id, actualizado);

        assertEquals(HttpStatus.OK, resp.getStatusCode());

        // Comprobar que se ha actualizado correctamente
        assertNotNull(resp.getBody());
        assertEquals("Nombre nuevo", resp.getBody().getNombre());
        assertEquals(20, resp.getBody().getEdad());
        assertEquals("nuevo@gmail.com", resp.getBody().getCorreo());
    }

    @Test
    void cuandoSeActualizaCorrectamenteParcial_devuelve200() {
        Long id = 1L;

        // Crear nuevo estudiante
        Estudiante existente = new Estudiante();
        existente.setNombre("Nombre viejo");
        existente.setEdad(18);
        existente.setCorreo("viejo@gmail.com");
        existente.setDeleted(false);

        // Actualizar los datos del estudiante creado
        Estudiante actualizado = new Estudiante();
        actualizado.setNombre("");
        actualizado.setEdad(20);
        actualizado.setCorreo("");
        existente.setDeleted(false);

        when(repository.findById(id)).thenReturn(Optional.of(existente));

        when(service.guardarEstudiante(any(Estudiante.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Código de respuesta
        ResponseEntity<Estudiante> resp = controller.actualizarEstudianteParcialmente(id, actualizado);

        assertEquals(HttpStatus.OK, resp.getStatusCode());

        // Comprobar que se ha actualizado correctamente
        assertNotNull(resp.getBody());
        assertEquals("Nombre viejo", resp.getBody().getNombre());
        assertEquals(20, resp.getBody().getEdad());
        assertEquals("viejo@gmail.com", resp.getBody().getCorreo());
    }

    @Test
    void borradoSoft_200() {
        Long id = 1L;

        // Crear nuevo estudiante
        Estudiante existente = new Estudiante();
        existente.setNombre("Nombre viejo");
        existente.setEdad(18);
        existente.setCorreo("viejo@gmail.com");
        existente.setDeleted(false);

        when(repository.findById(id)).thenReturn(Optional.of(existente));

        when(service.guardarEstudiante(any(Estudiante.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Código de respuesta
        ResponseEntity<Estudiante> resp = controller.actualizarEstadoEliminarSoft(id);

        assertEquals(HttpStatus.OK, resp.getStatusCode());

        // Comprobar que se ha actualizado correctamente
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isDeleted());
    }

    @Test
    void deshacerBorradoSoft_200() {
        Long id = 1L;

        // Crear nuevo estudiante
        Estudiante existente = new Estudiante();
        existente.setNombre("Nombre viejo");
        existente.setEdad(18);
        existente.setCorreo("viejo@gmail.com");
        existente.setDeleted(true); // En este caso está borrado, para poder realizar el resto correctamente

        when(repository.findById(id)).thenReturn(Optional.of(existente));

        when(service.guardarEstudiante(any(Estudiante.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Código de respuesta
        ResponseEntity<Estudiante> resp = controller.deshacerEliminadoSoft(id);

        assertEquals(HttpStatus.OK, resp.getStatusCode());

        // Comprobar que se ha actualizado correctamente
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isDeleted());
    }

    @Test
    void borrarEstudiante_200() {
        Long id = 1L;

        ResponseEntity<Void> resp = controller.eliminarEstudianteHard(id);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        assertNull(resp.getBody());

        verify(service).eliminarEstudiante(id);
        verifyNoMoreInteractions(service);
    }
}