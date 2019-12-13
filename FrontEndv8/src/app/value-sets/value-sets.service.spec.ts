import { TestBed } from '@angular/core/testing';

import { ValueSetsService } from './value-sets.service';

describe('ValueSetsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ValueSetsService = TestBed.get(ValueSetsService);
    expect(service).toBeTruthy();
  });
});
