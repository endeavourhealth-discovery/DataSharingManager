import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganisationPickerComponent } from './organisation-picker.component';

describe('OrganisationPickerComponent', () => {
  let component: OrganisationPickerComponent;
  let fixture: ComponentFixture<OrganisationPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OrganisationPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganisationPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
