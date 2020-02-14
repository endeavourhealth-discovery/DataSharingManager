import {Component, OnInit, ViewChild} from '@angular/core';
import {ReportingService} from "../reporting.service";
import {DataProcessingAgreementService} from "../../data-processing-agreement/data-processing-agreement.service";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Organisation} from "../../organisation/models/Organisation";
import {ReportData} from "../models/ReportData";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Project} from "../../project/models/Project";
import {DataSharingAgreementService} from "../../data-sharing-agreement/data-sharing-agreement.service";
import {ProjectService} from "../../project/project.service";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {GenericTableComponent, ItemLinkageService, LoggerService, UserManagerService} from "dds-angular8";
import {ngxCsv} from "ngx-csv";

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  userId: string;
  dpaLoadingComplete = false;
  dsaLoadingComplete = false;
  projectLoadingComplete = false;
  reportComplete = true;
  reportData: ReportData[];
  sortReverse = true;
  sortField = 'practiceName';
  reportName: string;
  public activeProject: UserProject;

  dpaDetailsToShow = [{label: 'Name', property: 'name'}];
  reportDetailsToShow = new ReportData().getDisplayItems();

  @ViewChild('dpaTable', { static: false }) dpaTable: GenericTableComponent;
  @ViewChild('dsaTable', { static: false }) dsaTable: GenericTableComponent;
  @ViewChild('projectTable', { static: false }) projectTable: GenericTableComponent;

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
    useTextFile: false,
    useBom: false,
  };

  constructor(private reportingService: ReportingService,
              private userManagerService: UserManagerService,
              private dpaService: DataProcessingAgreementService,
              private dsaService: DataSharingAgreementService,
              private projectService: ProjectService,
              private log: LoggerService,
              private itemLinkageService: ItemLinkageService) { }

  ngOnInit() {
    this.userManagerService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {


    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      this.userId = null;
    } else {
      this.userId = this.activeProject.userId;
    }

    this.getAvailableReports();
  }

  getAvailableReports() {

    this.getDPAs();
    this.getDsas();
    this.getProjects();
  }

  getDPAs() {

    this.dpaLoadingComplete = false;
    this.dpaService.getAllDpas(this.userId)
      .subscribe(
        result => {
          this.dpas = result;
          this.dpaLoadingComplete = true;
        },
        error => {
          this.log.error('The data processing agreements could not be loaded. Please try again.');
          this.dpaLoadingComplete = true;
          this.reportComplete = true;
        }
      );
  }

  getDsas() {

    this.dsaLoadingComplete = false;
    this.dsaService.getAllDsas(this.userId)
      .subscribe(
        result => {
          this.dsas = result;
          this.dsaLoadingComplete = true;
        },
        error => {
          this.log.error('The data sharing agreements could not be loaded. Please try again.');
          this.dsaLoadingComplete = true;
        }
      );
  }

  getProjects() {

    this.projectLoadingComplete = false;
    this.projectService.getAllProjects(this.userId)
      .subscribe(
        result => {
          this.projects = result;
          this.projectLoadingComplete = true;
        },
        error => {
          this.log.error('The projects could not be loaded. Please try again.');
          this.projectLoadingComplete = true;
        }
      );
  }

  clearDPASelection() {
    this.reportData = null;
    this.dpaTable.clearHighlights();
  }

  clearDSASelection() {
    this.reportData = null;
    this.dsaTable.clearHighlights();
  }

  clearProjectSelection() {
    this.reportData = null;
    this.projectTable.clearHighlights();
  }

  runDPAPublisherReport(dpa: Dpa) {

    this.clearDSASelection();
    this.clearProjectSelection();

    this.reportComplete = false;

    this.reportName = dpa.name + ' : Publisher Report';
    this.dpaService.getPublishers(dpa.uuid)
      .subscribe(
        result => {
          this.getDDSInformation(result, dpa.name);
        },
        error => {
          this.log.error('The associated publishers could not be loaded. Please try again.');
          this.reportComplete = true;
        }
      );
  }

  runDSAPublisherReport(dsa: Dsa) {
    this.clearDPASelection();
    this.clearProjectSelection();

    this.reportComplete = false;
    this.reportName = dsa.name + ' : Publisher Report';
    this.dsaService.getPublishers(dsa.uuid)
      .subscribe(
        result => {
          this.getDDSInformation(result, dsa.name);
        },
        error => {
          this.log.error('The associated publishers could not be loaded. Please try again.');
          this.reportComplete = true;
        }
      );
  }

  runProjectPublisherReport(project: Project) {
    this.clearDSASelection();
    this.clearDPASelection();

    this.reportComplete = false;
    this.reportName = project.name + ' : Publisher Report';
    this.projectService.getLinkedPublishers(project.uuid)
      .subscribe(
        result => {
          this.getDDSInformation(result, project.name);
        },
        error => {
          this.log.error('The associated publishers could not be loaded. Please try again.');
          this.reportComplete = true;
        }
      );
  }

  getDDSInformation(orgs: Organisation[], agreementName: string) {

    if (orgs.length === 0) {
      this.log.error('The selected report has no organisations associated with it.');
      return;
    }
    this.reportingService.getPublisherReport(orgs, agreementName)
      .subscribe(
        result => {
          this.reportData = result;
          console.log(result);
          this.getLinkedItemsForDPAReport();
          this.reportComplete = true;
        },
        error => {
          this.log.error('The report could not be run. Please try again.');
          this.reportComplete = true;
        }
      )
  }

  getLinkedItemsForDPAReport() {
    for (let rep of this.reportData) {
      for (let det of this.reportDetailsToShow) {
        if (det.link) {
          if (rep && rep[det.property]) {
            rep[det.property] = this.itemLinkageService.getLinkedItem(+rep[det.property], det.link);
          }
        }
      }
    }
  }

  exportToCSV() {
    new ngxCsv(this.reportData, 'generated', this.options);
  }
}

