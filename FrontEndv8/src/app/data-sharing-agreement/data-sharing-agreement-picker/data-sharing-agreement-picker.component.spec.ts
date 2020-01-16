import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSharingAgreementPickerComponent } from './data-sharing-agreement-picker.component';

describe('DataSharingAgreementPickerComponent', () => {
  let component: DataSharingAgreementPickerComponent;
  let fixture: ComponentFixture<DataSharingAgreementPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataSharingAgreementPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSharingAgreementPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
