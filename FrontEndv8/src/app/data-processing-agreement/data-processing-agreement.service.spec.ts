import { TestBed, inject } from '@angular/core/testing';

import { DataProcessingAgreementService } from './data-processing-agreement.service';

describe('DataProcessingAgreementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DataProcessingAgreementService]
    });
  });

  it('should be created', inject([DataProcessingAgreementService], (service: DataProcessingAgreementService) => {
    expect(service).toBeTruthy();
  }));
});
