import { Component, OnInit } from '@angular/core';
import {ReportingService} from "../reporting.service";
import {LoggerService, UserManagerNotificationService} from "eds-angular4";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";
import {DataProcessingAgreementService} from "../../data-processing-agreement/data-processing-agreement.service";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Organisation} from "../../organisation/models/Organisation";
import {ReportData} from "../models/ReportData";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Project} from "../../project/models/Project";
import {DataSharingAgreementService} from "../../data-sharing-agreement/data-sharing-agreement.service";
import {ProjectService} from "../../project/project.service";

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  allowEdit = false;
  allowBulk = false;
  dpaLoadingComplete = false;
  dsaLoadingComplete = false;
  projectLoadingComplete = false;
  reportComplete = true;
  reportData: ReportData[];
  sortReverse = true;
  sortField = 'practiceName';
  reportName = 'Publisher Report';
  public activeProject: UserProject;

  dpas: Dpa[];
  dsas: Dsa[];
  projects: Project[];

  options = {
    fieldSeparator: ',',
    quoteStrings: '"',
    decimalseparator: '.',
    showLabels: true,
    headers: ['Practice Name', 'ODS Code', 'CCG', 'Agreement', 'Last Received', 'In Error'],
    showTitle: false,
    title: 'Publisher Report',
    useBom: false,
    removeNewLines: true,
    keys: ['approved','age','name' ]
  };

  constructor(private reportingService: ReportingService,
              private userManagerNotificationService: UserManagerNotificationService,
              private dpaService: DataProcessingAgreementService,
              private dsaService: DataSharingAgreementService,
              private projectService: ProjectService,
              private log: LoggerService) { }

  ngOnInit() {
    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    const vm = this;

    vm.allowEdit = false;
    vm.allowBulk = false;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    }

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Config') != null) {
      vm.allowBulk = true;
    }

    vm.getAvailableReports();
  }

  getAvailableReports() {
    const vm = this;
    vm.getDPAs();
    vm.getDsas();
    vm.getProjects();
  }

  getDPAs() {
    const vm = this;
    vm.dpaLoadingComplete = false;
    vm.dpaService.getAllDpas()
      .subscribe(
        result => {
          vm.dpas = result;
          vm.dpaLoadingComplete = true;
        },
        error => {
          vm.log.error('The data processing agreements could not be loaded. Please try again.', error, 'Load data processing agreements');
          vm.dpaLoadingComplete = true;
          vm.reportComplete = true;
        }
      );
  }

  getDsas() {
    const vm = this;
    vm.dsaLoadingComplete = false;
    vm.dsaService.getAllDsas()
      .subscribe(
        result => {
          vm.dsas = result;
          vm.dsaLoadingComplete = true;
        },
        error => {
          vm.log.error('The data sharing agreements could not be loaded. Please try again.', error, 'Load data sharing agreements');
          vm.dsaLoadingComplete = true;
        }
      );
  }

  getProjects() {
    const vm = this;
    vm.projectLoadingComplete = false;
    vm.projectService.getAllProjects()
      .subscribe(
        result => {
          vm.projects = result;
          vm.projectLoadingComplete = true;
        },
        error => {
          vm.log.error('The projects could not be loaded. Please try again.', error, 'Load projects');
          vm.projectLoadingComplete = true;
        }
      );
  }

  runDPAPublisherReport(dpa: Dpa) {
    const vm = this;
    vm.reportComplete = false;
    vm.reportName = dpa.name + ' : ' + vm.reportName;
    vm.dpaService.getPublishers(dpa.uuid)
      .subscribe(
        result => {
          vm.getDDSInformation(result, dpa.name);
        },
        error => {
          vm.log.error('The associated publishers could not be loaded. Please try again.', error, 'Load publishers');
          vm.reportComplete = true;
        }
      );
  }

  runDSAPublisherReport(dsa: Dsa) {
    const vm = this;
    vm.reportComplete = false;
    vm.reportName = dsa.name + ' : ' + vm.reportName;
    vm.dsaService.getPublishers(dsa.uuid)
      .subscribe(
        result => {
          vm.getDDSInformation(result, dsa.name);
        },
        error => {
          vm.log.error('The associated publishers could not be loaded. Please try again.', error, 'Load publishers');
          vm.reportComplete = true;
        }
      );
  }

  runProjectPublisherReport(project: Project) {
    const vm = this;
    vm.reportComplete = false;
    vm.reportName = project.name + ' : ' + vm.reportName;
    vm.projectService.getLinkedPublishers(project.uuid)
      .subscribe(
        result => {
          vm.getDDSInformation(result, project.name);
        },
        error => {
          vm.log.error('The associated publishers could not be loaded. Please try again.', error, 'Load publishers');
          vm.reportComplete = true;
        }
      );
  }

  getDDSInformation(orgs: Organisation[], agreementName: string) {
    const vm = this;
    vm.reportingService.getPublisherReport(orgs, agreementName)
      .subscribe(
        result => {
          console.log(result);
          vm.reportData = result;
          vm.sort('practiceName');
          vm.reportComplete = true;
        },
        error => {
          vm.log.error('The report could not be run. Please try again.', error, 'Run report');
          vm.reportComplete = true;
        }
      )
  }

  sort(property: string) {
    const vm = this;
    vm.sortField = property;
    vm.sortReverse = !vm.sortReverse;

    vm.reportData.sort(function(a, b) {
      var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
      if (vm.sortReverse) {
        return result * -1;
      } else {
        return result;
      }
    })
  }

  exportToCSV() {
    const vm = this;
    // new Angular2Csv(vm.reportData, vm.reportName, vm.options);
  }

}
