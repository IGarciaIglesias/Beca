package com.example.demo;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/estudiantes")
public class ServidorPruebaDbController {

    private final EstudianteService service;
    private final EstudianteRepository repository;

    public ServidorPruebaDbController(EstudianteService service, EstudianteRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    // Método get para obtener la lista entera de estudiantes de la base de datos
    @GetMapping
    public ResponseEntity<List<Estudiante>> obtenerTodos() {
        List<Estudiante> estudiantes = service.buscarEstudiante();
        if (estudiantes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(estudiantes);
    }

    // Método get para obtener la lista de estudiantes cuyo campo "deleted" sea false
    @GetMapping("/noEliminados")
    public ResponseEntity<List<Estudiante>> obtenerNoEliminados() {
        List<Estudiante> estudiantes = service.buscarEstudiantesNoBorrados();
        if (estudiantes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(estudiantes);
    }

    // Método get para obtener la lista de estudiantes cuyo campo "deleted" sea true
    @GetMapping("/eliminados")
    public ResponseEntity<List<Estudiante>> obtenerEliminados() {
        List<Estudiante> estudiantes = service.buscarEstudiantesBorrados();
        if (estudiantes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(estudiantes);
    }

    // Método post para crear un nuevo registro
    @PostMapping("/crear")
    public ResponseEntity<Estudiante> crarEstudiante(@RequestBody Estudiante estudiante) {
        Estudiante nuevoEstudiante = service.guardarEstudiante(estudiante);
        return ResponseEntity.ok(nuevoEstudiante);
    }

    // Método put para editar un registro, en este caso es el registro completo
    @PutMapping("/completo/{id}")
    public ResponseEntity<Estudiante> actualizarEstudiante(
            @PathVariable Long id,
            @RequestBody Estudiante estudianteActualizado){

        return repository.findById(id)
                .map(estudiante -> {
                    estudiante.setNombre(estudianteActualizado.getNombre());
                    estudiante.setEdad(estudianteActualizado.getEdad());
                    estudiante.setCorreo(estudianteActualizado.getCorreo());
                    return ResponseEntity.ok(service.guardarEstudiante(estudiante));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Método patch para editar un registro, en este caso parcialmente
    @PatchMapping("/parcial/{id}")
    public ResponseEntity<Estudiante> actualizarEstudianteParcialmente(
            @PathVariable Long id,
            @RequestBody Estudiante edicionParcialEstudiante) {

        Optional<Estudiante> existingUserOpt = repository.findById(id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Estudiante estudianteExistente = existingUserOpt.get();

        String noEditar = " ";
        if (edicionParcialEstudiante.getNombre().equals(noEditar)) {
            estudianteExistente.setNombre(edicionParcialEstudiante.getNombre());
        }
        if (edicionParcialEstudiante.getCorreo().equals(noEditar)) {
            estudianteExistente.setCorreo(edicionParcialEstudiante.getCorreo());
        }
        if (edicionParcialEstudiante.getEdad() != 0) {
            estudianteExistente.setEdad(edicionParcialEstudiante.getEdad());
        }

        Estudiante updatedUser = service.guardarEstudiante(estudianteExistente);
        return ResponseEntity.ok(updatedUser);
    }

    // Método put para el deshacer el borradoSoft, en este caso no se elimina de la base de datos
    // únicamente pasa a estar como verdadero el campo deleted
    @PutMapping("/borradoSoft/{id}")
    public ResponseEntity<Estudiante> actualizarEstadoEliminarSoft(
            @PathVariable Long id){

        List<Estudiante> estudiantes = service.buscarEstudiantesBorrados();
        if (!estudiantes.isEmpty()) {
            return repository.findById(id)
                    .map(estudiante -> {
                        estudiante.setDeleted(true);
                        return ResponseEntity.ok(service.guardarEstudiante(estudiante));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    // Método put para el deshacer el borradoSoft, se devuelve el registro a su estado inicial
    @PutMapping("/devolverEstado/{id}")
    public ResponseEntity<Estudiante> deshacerEliminadoSoft(
            @PathVariable Long id){

        List<Estudiante> estudiantes = service.buscarEstudiantesBorrados();
        if (!estudiantes.isEmpty()) {
            return repository.findById(id)
                    .map(estudiante -> {
                        estudiante.setDeleted(false);
                        return ResponseEntity.ok(service.guardarEstudiante(estudiante));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/borradoHard/{id}")
    public ResponseEntity<Void> eliminarEstudianteHard(@PathVariable Long id){
        service.eliminarEstudiante(id);
        return ResponseEntity.notFound().<Void>build();
    }
}
