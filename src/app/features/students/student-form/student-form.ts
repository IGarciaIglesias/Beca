import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { StudentService } from '../../../core/api/student.service';
import { ApiValidationError } from '../../../core/api/api-error.model';

@Component({
  selector: 'app-student-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './student-form.html',
  styleUrls: ['./student-form.css']
})
export class StudentFormComponent implements OnInit {
  id?: number;
  serverErrors: Record<string, string> = {};
  saving = false;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private api: StudentService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // ✅ inicialización aquí, cuando fb ya existe
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      age: [null, [Validators.required, Validators.min(1)]],
      correo: ['', [Validators.required, Validators.email]],
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.api.get(this.id).subscribe({
        next: s => this.form.patchValue({ name: s.name, age: s.age, correo: s.correo })
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
    const payload = this.form.getRawValue() as any;

    const req$ = this.id
      ? this.api.replace(this.id, payload)
      : this.api.create(payload);

    req$.subscribe({
      next: (saved) => {
        this.saving = false;
        this.router.navigate(['/students', saved.id]);
      },
      error: (err) => {
        this.saving = false;
        const body = err?.error as ApiValidationError;
        if (body?.errors) this.serverErrors = body.errors;
      }
    });
  }
}
