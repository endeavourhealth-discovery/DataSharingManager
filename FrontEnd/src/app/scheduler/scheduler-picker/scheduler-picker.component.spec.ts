import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SchedulerPickerComponent } from './scheduler-picker.component';

describe('SchedulerPickerComponent', () => {
  let component: SchedulerPickerComponent;
  let fixture: ComponentFixture<SchedulerPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SchedulerPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SchedulerPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
