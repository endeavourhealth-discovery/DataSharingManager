import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CohortPickerComponent } from './cohort-picker.component';

describe('CohortPickerComponent', () => {
  let component: CohortPickerComponent;
  let fixture: ComponentFixture<CohortPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CohortPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CohortPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
