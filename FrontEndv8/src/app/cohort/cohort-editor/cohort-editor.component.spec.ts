import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CohortEditorComponent } from './cohort-editor.component';

describe('CohortEditorComponent', () => {
  let component: CohortEditorComponent;
  let fixture: ComponentFixture<CohortEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CohortEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CohortEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
