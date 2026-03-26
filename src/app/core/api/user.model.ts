export type UserRole = 'ADMIN' | 'GESTOR' | 'USER';

export interface User {
  id: number;
  correo: string;
  role: UserRole;
}
``