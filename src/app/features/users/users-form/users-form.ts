import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { UserService } from '../../../core/api/user.service';
import { User, UserRole } from '../../../core/api/user.model';
import { ApiValidationError } from '../../../core/api/api-error.model';

@Component({
  selector: 'app-users-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './users-form.html'
})
export class UsersFormComponent implements OnInit {

  id?: number;
  saving = false;
  serverErrors: Record<string, string> = {};
  form!: FormGroup;

  roles: UserRole[] = ['ADMIN', 'GESTOR', 'USER'];

  constructor(
    private fb: FormBuilder,
    private api: UserService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      role: ['USER' as UserRole, Validators.required]
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.api.get(this.id).subscribe({
        next: (u: User) => {
          this.form.patchValue({
            correo: u.correo,
            role: u.role
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
    const payload = this.form.getRawValue() as User;

    const req$ = this.id
      ? this.api.replace(this.id, payload)
      : this.api.create(payload);

    req$.subscribe({
      next: saved => {
        this.saving = false;
        this.router.navigate(['/users', saved.id]);
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