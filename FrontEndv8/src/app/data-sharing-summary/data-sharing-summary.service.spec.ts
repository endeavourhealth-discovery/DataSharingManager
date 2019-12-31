import { TestBed, inject } from '@angular/core/testing';

import { DataSharingSummaryService } from './data-sharing-summary.service';

describe('DataSharingSummaryService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DataSharingSummaryService]
    });
  });

  it('should be created', inject([DataSharingSummaryService], (service: DataSharingSummaryService) => {
    expect(service).toBeTruthy();
  }));
});
