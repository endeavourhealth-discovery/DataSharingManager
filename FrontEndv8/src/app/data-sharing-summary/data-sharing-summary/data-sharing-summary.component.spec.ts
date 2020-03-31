import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSharingSummaryComponent } from './data-sharing-summary.component';

describe('DataSharingSummaryComponent', () => {
  let component: DataSharingSummaryComponent;
  let fixture: ComponentFixture<DataSharingSummaryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataSharingSummaryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSharingSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
