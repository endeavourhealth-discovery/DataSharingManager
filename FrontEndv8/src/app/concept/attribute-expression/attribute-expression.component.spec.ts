import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeExpressionComponent } from './attribute-expression.component';

describe('AttributeExpressionComponent', () => {
  let component: AttributeExpressionComponent;
  let fixture: ComponentFixture<AttributeExpressionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AttributeExpressionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeExpressionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
