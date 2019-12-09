import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConceptExpressionComponent } from './concept-expression.component';

describe('ConceptExpressionComponent', () => {
  let component: ConceptExpressionComponent;
  let fixture: ComponentFixture<ConceptExpressionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConceptExpressionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConceptExpressionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
