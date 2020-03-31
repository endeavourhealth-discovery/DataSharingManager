import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataProcessingAgreementComponent } from './data-processing-agreement.component';

describe('DataProcessingAgreementComponent', () => {
  let component: DataProcessingAgreementComponent;
  let fixture: ComponentFixture<DataProcessingAgreementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataProcessingAgreementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataProcessingAgreementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
