import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { ProfesorService } from '../../../core/api/profesor.service';
import { Profesor } from '../../../core/api/profesor.model';
import { ApiValidationError } from '../../../core/api/api-error.model';

@Component({
  selector: 'app-profesor-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './profesor-form.html',
    styleUrls: ['./profesor-form.css']
})
export class ProfesorFormComponent implements OnInit {

  id?: number;
  saving = false;
  serverErrors: Record<string, string> = {};
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private api: ProfesorService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      correo: ['', [Validators.required, Validators.email]]
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.api.get(this.id).subscribe({
        next: (p: Profesor) => {
          this.form.patchValue({
            correo: p.correo
          });
        }
      });
    }
  }

  submit() {
    this.serverErrors = {};

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const payload = this.form.getRawValue() as Profesor;

    const req$ = this.id
      ? this.api.replace(this.id, payload)
      : this.api.create(payload);

    req$.subscribe({
      next: saved => {
        this.saving = false;
        this.router.navigate(['/profesores', saved.id]);
      },
      error: err => {
        this.saving = false;
        const body = err?.error as ApiValidationError;
        if (body?.errors) {
          this.serverErrors = body.errors;
        }
      }
    });
  }
}