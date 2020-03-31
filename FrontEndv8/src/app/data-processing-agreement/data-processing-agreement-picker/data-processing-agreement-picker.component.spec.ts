import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataProcessingAgreementPickerComponent } from './data-processing-agreement-picker.component';

describe('DataProcessingAgreementPickerComponent', () => {
  let component: DataProcessingAgreementPickerComponent;
  let fixture: ComponentFixture<DataProcessingAgreementPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataProcessingAgreementPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataProcessingAgreementPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
