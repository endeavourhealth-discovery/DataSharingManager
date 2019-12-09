import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConceptEditorComponent } from './concept-editor.component';

describe('ConceptEditorComponent', () => {
  let component: ConceptEditorComponent;
  let fixture: ComponentFixture<ConceptEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConceptEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConceptEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
