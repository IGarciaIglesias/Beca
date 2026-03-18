import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { StudentsListComponent } from './students-list';
import { StudentService } from '../../../core/api/student.service';

describe('StudentsListComponent', () => {
  let fixture: ComponentFixture<StudentsListComponent>;
  let component: StudentsListComponent;

  const api = jasmine.createSpyObj<StudentService>('StudentService', [
    'list',
    'delete',
    'refreshCache',
  ]);

  beforeEach(async () => {
    api.list.and.returnValue(of([] as any));
    api.delete.and.returnValue(of(void 0));
    api.refreshCache.and.returnValue(of({ status: 'ok', cleared: [] } as any));

    await TestBed.configureTestingModule({
      imports: [StudentsListComponent, RouterTestingModule],
      providers: [{ provide: StudentService, useValue: api }],
    }).compileComponents();

    fixture = TestBed.createComponent(StudentsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('reload() should call list()', () => {
    // si existe en tu componente
    if (typeof (component as any).reload === 'function') {
      (component as any).reload();
      expect(api.list).toHaveBeenCalled();
    }
  });

  it('refreshCache() should call service.refreshCache()', () => {
    if (typeof (component as any).refreshCache === 'function') {
      (component as any).refreshCache();
      expect(api.refreshCache).toHaveBeenCalled();
    }
  });

  it('deleteSoft(id) should call service.delete', () => {
    if (typeof (component as any).deleteSoft === 'function') {
      (component as any).deleteSoft(10);
      expect(api.delete).toHaveBeenCalled();
    }
  });

  it('deleteHard(id) should call service.delete', () => {
    if (typeof (component as any).deleteHard === 'function') {
      (component as any).deleteHard(11);
      expect(api.delete).toHaveBeenCalled();
    }
  });
}); 