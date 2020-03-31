import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSharingAgreementComponent } from './data-sharing-agreement.component';

describe('DataSharingAgreementComponent', () => {
  let component: DataSharingAgreementComponent;
  let fixture: ComponentFixture<DataSharingAgreementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataSharingAgreementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSharingAgreementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
