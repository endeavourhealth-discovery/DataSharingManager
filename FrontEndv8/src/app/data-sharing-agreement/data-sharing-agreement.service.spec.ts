import { TestBed, inject } from '@angular/core/testing';

import { DataSharingAgreementService } from './data-sharing-agreement.service';

describe('DataSharingAgreementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DataSharingAgreementService]
    });
  });

  it('should be created', inject([DataSharingAgreementService], (service: DataSharingAgreementService) => {
    expect(service).toBeTruthy();
  }));
});
