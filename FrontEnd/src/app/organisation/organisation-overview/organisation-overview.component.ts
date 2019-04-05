import {Component, Input, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Router} from '@angular/router';
import {Organisation} from '../models/Organisation';
import {OrganisationService} from '../organisation.service';
import {OrganisationManagerStatistics} from '../models/OrganisationManagerStatistics';
import {FileUpload} from '../models/FileUpload';
import {LoggerService, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import { ToastsManager } from 'ng2-toastr/ng2-toastr';
import {User} from 'eds-angular4/dist/security/models/User';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-organisation-overview',
  templateUrl: './organisation-overview.component.html',
  styleUrls: ['./organisation-overview.component.css']
})
export class OrganisationOverviewComponent implements OnInit {
  organisations: Organisation[];
  allowEdit = false;
  allowConfig = false;

  public activeProject: UserProject;

  orgStats: OrganisationManagerStatistics[];
  orgLoadingComplete = false;
  serviceStats: OrganisationManagerStatistics[];
  serviceLoadingComplete = false;
  regionStats: OrganisationManagerStatistics[];
  regionLoadingComplete = false;
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
  projectStats: OrganisationManagerStatistics[];
  projectLoadingComplete = false;

  constructor(private $modal: NgbModal,
              private organisationService: OrganisationService,
              private log: LoggerService,
              private securityService: SecurityService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

    this.getOverview();
  }

  roleChanged() {
    const vm = this;

    vm.allowEdit = false;
    vm.allowConfig = false;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    }

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Config') != null) {
      vm.allowConfig = true;
    }
  }

  getOverview() {
    const vm = this;
    vm.getOrganisationStatistics();
    vm.getServiceStatistics();
    vm.getRegionStatistics();
    vm.getDpaStatistics();
    vm.getDsaStatistics();
    vm.getDataFlowStatistics();
    vm.getCohortStatistics();
    vm.getDataSetStatistics();
    vm.getProjectStatistics();
  }

  getOrganisationStatistics() {
    const vm = this;
    vm.orgLoadingComplete = false;
    vm.organisationService.getStatistics('organisation')
      .subscribe(result => {
          vm.orgStats = result;
          vm.orgLoadingComplete = true;
        },
        error => {
          vm.log.error('Failed to load organisation statistics', error, 'Load service statistics');
          vm.orgLoadingComplete = true;
        }
      );
  }

  getServiceStatistics() {
    const vm = this;
    vm.serviceLoadingComplete = false;
    vm.organisationService.getStatistics('service')
      .subscribe(result => {
          vm.serviceStats = result;
          vm.serviceLoadingComplete = true;
        },
        error => {
          vm.log.error('Failed to load service statistics', error, 'Load service statistics');
          vm.serviceLoadingComplete = true;
        }
      );
  }

  getRegionStatistics() {
    const vm = this;
    vm.regionLoadingComplete = false;
    vm.organisationService.getStatistics('region')
      .subscribe(result => {
          vm.regionStats = result;
          vm.regionLoadingComplete = true;
        },
        error => {
          vm.log.error('Failed to load region statistics', error, 'Load region statistics');
          vm.regionLoadingComplete = true;
        }
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

  getProjectStatistics() {
    const vm = this;
    vm.projectLoadingComplete = false;
    vm.organisationService.getStatistics('project')
      .subscribe(result => {
          vm.projectStats = result;
          vm.projectLoadingComplete = true;
        },
        error => {
          vm.log.error('The project statistics could not be loaded. Please try again.', error, 'Load project statistics');
          vm.projectLoadingComplete = true;
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

  goToOrganisations() {
    this.router.navigate(['/organisations', {mode: 'organisations'}]);
  }

  goToConfig() {
    this.router.navigate(['/configuration']);
  }

  goToServices() {
    this.router.navigate(['organisations', {mode: 'services'}]);
  }

  goToRegions() {
    this.router.navigate(['/regions']);
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

  goToProjects() {
    this.router.navigate(['/projects']);
  }
}
