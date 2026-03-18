import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { StudentService } from './student.service';
import { Student } from './student.model';

describe('StudentService', () => {
  let service: StudentService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [StudentService],
    });
    service = TestBed.inject(StudentService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('list() should GET /api/students', () => {
    service.list().subscribe();
    const req = http.expectOne('/api/students');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('get(id) should GET /api/students/:id', () => {
    service.get(7).subscribe();
    const req = http.expectOne('/api/students/7');
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7 } as any);
  });

  it('create(student) should POST /api/students', () => {
    const payload = { name: 'A', age: 1, correo: 'a@a.com', role: 'USER' } as Student;

    service.create(payload).subscribe();
    const req = http.expectOne('/api/students');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ ...payload, id: 1 });
  });

  it('replace(id, student) should PUT /api/students/:id', () => {
    const payload = { name: 'B', age: 2, correo: 'b@b.com', role: 'ADMIN' } as Student;

    service.replace(9, payload).subscribe();
    const req = http.expectOne('/api/students/9');
    expect(req.request.method).toBe('PUT');
    req.flush({ ...payload, id: 9 });
  });

  it('patch(id, fields) should PATCH /api/students/:id', () => {
    service.patch(3, { role: 'USER' } as any).subscribe();
    const req = http.expectOne('/api/students/3');
    expect(req.request.method).toBe('PATCH');
    req.flush({ id: 3 } as any);
  });

  it('delete(id, soft) should DELETE with soft param', () => {
    service.delete(5, true).subscribe();
    const req = http.expectOne(r => r.url === '/api/students/5' && r.params.get('soft') === 'true');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('refreshCache() should GET /api/students/refresh', () => {
    service.refreshCache().subscribe();
    const req = http.expectOne('/api/students/refresh');
    expect(req.request.method).toBe('GET');
    req.flush({ status: 'ok', cleared: [] });
  });
});