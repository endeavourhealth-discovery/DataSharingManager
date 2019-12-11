import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericTableSspComponent } from './generic-table-ssp.component';

describe('GenericTableSspComponent', () => {
  let component: GenericTableSspComponent;
  let fixture: ComponentFixture<GenericTableSspComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GenericTableSspComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GenericTableSspComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
