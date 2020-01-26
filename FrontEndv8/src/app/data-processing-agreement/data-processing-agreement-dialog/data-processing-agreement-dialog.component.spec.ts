import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataProcessingAgreementDialogComponent } from './data-processing-agreement-dialog.component';

describe('DataProcessingAgreementDialogComponent', () => {
  let component: DataProcessingAgreementDialogComponent;
  let fixture: ComponentFixture<DataProcessingAgreementDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataProcessingAgreementDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataProcessingAgreementDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
