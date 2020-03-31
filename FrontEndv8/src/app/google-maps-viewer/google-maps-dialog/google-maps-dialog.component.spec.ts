import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GoogleMapsDialogComponent } from './google-maps-dialog.component';

describe('GoogleMapsDialogComponent', () => {
  let component: GoogleMapsDialogComponent;
  let fixture: ComponentFixture<GoogleMapsDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GoogleMapsDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GoogleMapsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
