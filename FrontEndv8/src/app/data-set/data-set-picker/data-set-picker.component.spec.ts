import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSetPickerComponent } from './data-set-picker.component';

describe('DataSetPickerComponent', () => {
  let component: DataSetPickerComponent;
  let fixture: ComponentFixture<DataSetPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataSetPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSetPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
