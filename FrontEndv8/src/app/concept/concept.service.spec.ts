import { TestBed } from '@angular/core/testing';

import { ConceptService } from './concept.service';

describe('ConceptService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ConceptService = TestBed.get(ConceptService);
    expect(service).toBeTruthy();
  });
});
