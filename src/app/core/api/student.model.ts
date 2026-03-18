// src/app/core/api/student.model.ts

export type Role = 'ADMIN' | 'USER';

export interface Student {
  id?: number;
  name: string;
  age: number;
  correo: string;
  role: Role;
  deleted?: boolean;
}