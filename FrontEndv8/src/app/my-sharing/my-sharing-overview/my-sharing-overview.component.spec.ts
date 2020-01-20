import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganisationOverviewComponent } from './my-sharing-overview.component';

describe('OrganisationOverviewComponent', () => {
  let component: OrganisationOverviewComponent;
  let fixture: ComponentFixture<OrganisationOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OrganisationOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganisationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
