import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { EstudianteService, Estudiante } from './EstudianteService';
import { FormsModule } from '@angular/forms';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: `./app.html`,
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  estudiantes: Estudiante[] = [];
 
  mostrarModal = false;
  modoEditar = false;
  estudianteForm: Partial<Estudiante> = {};

  mostrarConfirmacion = false;
  estudianteSeleccionado: Estudiante | null = null;

  constructor(private estudianteService: EstudianteService) {}

  ngOnInit(): void {
    this.loadEstudiantes();
  }

  loadEstudiantes(){  
    this.estudianteService.getEstudiantes().subscribe({
      next: (response : Estudiante[]) => {
        this.estudiantes = response.sort((a, b) => a.id - b.id);
        this.cdr.detectChanges();
      }
    })
  }

  abrirModalCrear(){
    this.modoEditar = false;
    this.estudianteForm = { nombre: '', edad: undefined, correo: '', deleted: false };
    this.mostrarModal = true;
  }

  abrirModalEditar(estudiante: Estudiante){
    this.modoEditar = true;
    this.estudianteForm = { ...estudiante };
    this.mostrarModal = true;
  }

  guardar() {
    if (this.modoEditar){
      this.estudianteService.updateEstudiante(this.estudianteForm as Estudiante).subscribe({
        next: () => {
          this.loadEstudiantes();
          this.cerrarModal();
        }
      });
    } else {
      this.estudianteService.createEstudiante(this.estudianteForm as Estudiante).subscribe({
        next: () => {
          this.loadEstudiantes();
          this.cerrarModal();
        }
      });
    }
  }

  cerrarModal(){
    this.mostrarModal = false;
    this.estudianteForm = {};
  }

  abrirConfirmacion(estudiante: Estudiante){
    this.estudianteSeleccionado = estudiante;
    this.mostrarConfirmacion = true;
  }

  confirmarBorrar(){
    if(!this.estudianteSeleccionado) return;

    this.estudianteService.deleteEstudiante(this.estudianteSeleccionado.id).subscribe({
      next: () => {
        this.cerrarConfirmacion();
        this.loadEstudiantes();
      },
      error: () => {
        this.cerrarConfirmacion();
        this.loadEstudiantes();
      }
    })
  }

  cerrarConfirmacion(){
    this.mostrarConfirmacion = false;
    this.estudianteSeleccionado = null;
  }

  trackById(index: number, item: Estudiante): number{
    return item.id;
  }
}
