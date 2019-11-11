import { TestBed, inject } from '@angular/core/testing';

import { ExtractTechnicalDetailsService } from './extract-technical-details.service';

describe('ExtractTechnicalDetailsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExtractTechnicalDetailsService]
    });
  });

  it('should be created', inject([ExtractTechnicalDetailsService], (service: ExtractTechnicalDetailsService) => {
    expect(service).toBeTruthy();
  }));
});
