import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSharingAgreementDialogComponent } from './data-sharing-agreement-dialog.component';

describe('DataSharingAgreementDialogComponent', () => {
  let component: DataSharingAgreementDialogComponent;
  let fixture: ComponentFixture<DataSharingAgreementDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataSharingAgreementDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSharingAgreementDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
