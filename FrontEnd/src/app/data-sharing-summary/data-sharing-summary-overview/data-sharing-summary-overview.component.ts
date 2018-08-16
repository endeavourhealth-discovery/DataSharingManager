import { Component, OnInit } from '@angular/core';
import {OrganisationManagerStatistics} from '../../organisation/models/OrganisationManagerStatistics';
import {DataSharingSummaryService} from '../data-sharing-summary.service';
import {LoggerService} from 'eds-angular4';
import {Router} from "@angular/router";
import {OrganisationService} from "../../organisation/organisation.service";

@Component({
  selector: 'app-data-sharing-summary-overview',
  templateUrl: './data-sharing-summary-overview.component.html',
  styleUrls: ['./data-sharing-summary-overview.component.css']
})
export class DataSharingSummaryOverviewComponent implements OnInit {
  private file: File;

  summaryStats: OrganisationManagerStatistics[];
  dpaStats: OrganisationManagerStatistics[];
  dpaLoadingComplete = false;
  dsaStats: OrganisationManagerStatistics[];
  dsaLoadingComplete = false;
  dataflowStats: OrganisationManagerStatistics[];
  dataflowLoadingComplete = false;
  cohortStats: OrganisationManagerStatistics[];
  cohortLoadingComplete = false;
  datasetStats: OrganisationManagerStatistics[];
  datasetLoadingComplete = false;
  dataExchangeStats: OrganisationManagerStatistics[];
  exchangeLoadingComplete = false;

  constructor(private dataSharingSummaryService: DataSharingSummaryService,
              private organisationService: OrganisationService,
              private log: LoggerService,
              private router: Router) { }

  ngOnInit() {
    this.getOverview();
  }

  getOverview() {
    const vm = this;
    // vm.getSummaryStatistics();
    vm.getDpaStatistics();
    vm.getDsaStatistics();
    vm.getDataFlowStatistics();
    vm.getCohortStatistics();
    vm.getDataSetStatistics();
    vm.getSummaryStatistics();
    vm.getDataExchangeStatistics();
  }

  getSummaryStatistics() {
    const vm = this;
    vm.organisationService.getStatistics('summary')
      .subscribe(result => {
          vm.summaryStats = result
        },
        error => vm.log.error('The data sharing summary statistics could not be loaded. Please try again.', error, 'Load data sharing summary statistics')
      );
  }

  getDpaStatistics() {
    const vm = this;
    vm.dpaLoadingComplete = false;
    vm.organisationService.getStatistics('dpa')
      .subscribe(result => {
          vm.dpaStats = result;
          vm.dpaLoadingComplete = true;
        },
        error => {
        vm.log.error('The data processing agreement statistics could not be loaded. Please try again.', error, 'Load data processing agreement statistics');
        vm.dpaLoadingComplete = true;
        }
      );
  }

  getDsaStatistics() {
    const vm = this;
    vm.dsaLoadingComplete = false;
    vm.organisationService.getStatistics('dsa')
      .subscribe(result => {
          vm.dsaStats = result;
          vm.dsaLoadingComplete = true;
        },
        error => {
        vm.log.error('The data sharing agreement statistics could not be loaded. Please try again.', error, 'load data sharing agreement statistics');
        vm.dsaLoadingComplete = true;
        }
      );
  }

  getDataFlowStatistics() {
    const vm = this;
    vm.dataflowLoadingComplete = false;
    vm.organisationService.getStatistics('dataflow')
      .subscribe(result => {
          vm.dataflowStats = result;
          vm.dataflowLoadingComplete = true;
        },
        error => {
        vm.log.error('The data flow statistics could not be loaded. Please try again.', error, 'Load data flow statistics');
        vm.dataflowLoadingComplete = true;
        }
      );
  }

  getCohortStatistics() {
    const vm = this;
    vm.cohortLoadingComplete = false;
    vm.organisationService.getStatistics('cohort')
      .subscribe(result => {
          vm.cohortStats = result;
          vm.cohortLoadingComplete = true;
        },
        error => {
        vm.log.error('The cohort statistics could not be loaded. Please try again.', error, 'Load cohort statistics');
        vm.cohortLoadingComplete = true;
        }
      );
  }

  getDataSetStatistics() {
    const vm = this;
    vm.datasetLoadingComplete = false;
    vm.organisationService.getStatistics('dataset')
      .subscribe(result => {
          vm.datasetStats = result;
          vm.datasetLoadingComplete = true;
        },
        error => {
        vm.log.error('The data set statistics could not be loaded. Please try again.', error, 'Load data set statistics');
        vm.datasetLoadingComplete = true;
        }
      );
  }

  getDataExchangeStatistics() {
    const vm = this;
    vm.exchangeLoadingComplete = false;
    vm.organisationService.getStatistics('exchange')
      .subscribe(result => {
          vm.dataExchangeStats = result;
          vm.exchangeLoadingComplete = true;
        },
        error => {
        vm.log.error('The data exchange statistics could not be loaded. Please try again.', error, 'Load data exchange statistics');
        vm.exchangeLoadingComplete = true;
        }
      );
  }

  fileChange(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file = fileList[0];
    } else {
      this.file = null;
    }
  }

  goToSummary() {
    this.router.navigate(['/dataSharingSummaries']);
  }

  goToDpa() {
    this.router.navigate(['/dpas']);
  }

  goToDsa() {
    this.router.navigate(['/dsas']);
  }

  goToDataFlow() {
    this.router.navigate(['/dataFlows']);
  }

  goToCohorts() {
    this.router.navigate(['/cohorts']);
  }

  goToDataSets() {
    this.router.navigate(['/dataSets']);
  }

  goToDataExchanges() {
    this.router.navigate(['/dataExchanges']);
  }

}
