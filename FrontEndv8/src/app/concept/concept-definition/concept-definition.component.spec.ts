import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConceptDefinitionComponent } from './concept-definition.component';

describe('ConceptDefinitionComponent', () => {
  let component: ConceptDefinitionComponent;
  let fixture: ComponentFixture<ConceptDefinitionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConceptDefinitionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConceptDefinitionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
