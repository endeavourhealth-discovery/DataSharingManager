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
import {GenericTableComponent, ItemLinkageService, LoggerService, UserManagerService} from "dds-angular8";
import {ngxCsv} from "ngx-csv";
import {DatePipe} from "@angular/common";
import {UserProject} from "dds-angular8/user-manager";
import {Router} from "@angular/router";
import {SupplierTypeStatistic} from "../models/SupplierTypeStatistic";

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
  filteredReportData: ReportData[];
  activityReportData: any[];
  reportName: string;
  public activeProject: UserProject;
  parentType: number = 5;
  childType: number = 8;
  days: number = 2;
  totalOrgs: number;
  supplierCount: SupplierTypeStatistic[] = [];
  activatedCount: number;
  notActivatedCount: number;
  errorCount: number;
  noErrorCount: number;
  notReceivedRecently: number;
  receivedRecently: number;

  compareDate: Date = new Date();

  mapTypes = this.itemLinkageService.mapTypes;

  dpaDetailsToShow = [{label: 'Name', property: 'name'}];
  reportDetailsToShow = new ReportData().getDisplayItems();

  activityReportDetailsToShow = [
    {label: 'Parent', property: 'parentUuid'},
    {label: 'Child', property: 'childUuid'},
    {label: 'Date inserted', property: 'insertedAt'}
  ];

  @ViewChild('dpaTable', { static: false }) dpaTable: GenericTableComponent;
  @ViewChild('dsaTable', { static: false }) dsaTable: GenericTableComponent;
  @ViewChild('projectTable', { static: false }) projectTable: GenericTableComponent;
  @ViewChild('dsaPubTable', { static: false }) dsaPubTable: GenericTableComponent;

  dpas: Dpa[];
  dsas: Dsa[];
  projects: Project[];

  options = {
    fieldSeparator: ',',
    quoteStrings: '"',
    decimalseparator: '.',
    showLabels: true,
    headers: ['Practice name', 'ODS code', 'CCG', 'Agreement', 'Last received', 'In error',
      'System supplier', 'Supplier reference', 'Sharing activated'],
    showTitle: false,
    title: 'Publisher report',
    useTextFile: false,
    useBom: false,
  };

  activityReportOptions = {
    fieldSeparator: ',',
    quoteStrings: '"',
    decimalseparator: '.',
    showLabels: true,
    headers: ['Parent name', 'Child name', 'Date inserted'],
    showTitle: false,
    title: 'Activity report',
    useTextFile: false,
    useBom: false,
  };

  constructor(private reportingService: ReportingService,
              private userManagerService: UserManagerService,
              private dpaService: DataProcessingAgreementService,
              private dsaService: DataSharingAgreementService,
              private projectService: ProjectService,
              private log: LoggerService,
              private itemLinkageService: ItemLinkageService,
              private datePipe: DatePipe,
              private router: Router,) { }

  ngOnInit() {

    this.compareDate.setHours(this.compareDate.getHours() - 24);

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
    this.filteredReportData = null;
    this.dpaTable.clearHighlights();
  }

  clearDSASelection() {
    this.reportData = null;
    this.filteredReportData = null;
    this.dsaTable.clearHighlights();
  }

  clearProjectSelection() {
    this.reportData = null;
    this.filteredReportData = null;
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
    this.reportName = dsa.name + ' : Publisher report';
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

  organisationClicked(repData: ReportData) {
    window.open('#/organisation/' + repData.orgUUID + '/edit');
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
    this.reportData = [];
    this.supplierCount = [];
    this.reportingService.getPublisherReport(orgs, agreementName)
      .subscribe(
        result => {
          this.reportData = result;
          this.getLinkedItemsForDPAReport();
          this.filteredReportData = this.reportData;
          this.getStatisticsForPublisherReport();
          this.reportComplete = true;
        },
        error => {
          this.log.error('The report could not be run. Please try again.');
          this.reportComplete = true;
        }
      )
  }

  getStatisticsForPublisherReport() {

    this.totalOrgs = this.reportData.length;
    var orgsBySystemType = this.groupBy(this.reportData, (data) => data.systemSupplierType);

    for(let type of orgsBySystemType) {
      this.supplierCount.push(new SupplierTypeStatistic(type[0].systemSupplierType, type.length));
    }

    this.errorCount = this.reportData.filter((org) => org.inError).length;

    this.noErrorCount = this.reportData.filter((org) => !org.inError).length;

    this.activatedCount = this.reportData.filter((org) => org.sharingActivated === 'Yes').length;

    this.notActivatedCount = this.reportData.filter((org) => org.sharingActivated != 'Yes').length;

    console.log(this.compareDate);

    for (let org of this.reportData) {
      console.log(Date.parse(org.lastReceived));
    }

    this.notReceivedRecently = this.reportData.filter((org) => (new Date(org.lastReceived)) < this.compareDate).length;

    this.receivedRecently = this.reportData.filter((org) => (new Date(org.lastReceived)) >= this.compareDate).length;
  }

  groupBy<T, K>(list: T[], getKey: (item: T) => K) {
    const map = new Map<K, T[]>();
    list.forEach((item) => {
      const key = getKey(item);
      const collection = map.get(key);
      if (!collection) {
        map.set(key, [item]);
      } else {
        collection.push(item);
      }
    });
    return Array.from(map.values());
  }

  getLinkedItemsForDPAReport() {
    for (let rep of this.reportData) {
      for (let det of this.reportDetailsToShow) {
        if (det.link) {
          if (rep && rep[det.property] != null) {
            rep[det.property] = this.itemLinkageService.getLinkedItem(+rep[det.property], det.link);
          }
        }
      }
    }
  }

  exportToCSV() {

    // create a new object removing UUID as that doesn't need to be extracted
    var exportData = this.filteredReportData.map(({practiceName, odsCode, ccg, referenceAgreement, lastReceived, inError, systemSupplierType, systemSupplierReference, sharingActivated}) =>
      ({practiceName, odsCode, ccg, referenceAgreement, lastReceived, inError, systemSupplierType, systemSupplierReference, sharingActivated}));

    new ngxCsv(exportData, 'Publisher report', this.options);
  }

  exportActivityReportToCSV() {
    new ngxCsv(this.activityReportData, 'Activity report', this.activityReportOptions);
  }

  runActivityReport() {
    this.getRecentActivityReport(this.parentType, this.childType, this.days);
  }

  getRecentActivityReport(parentMapTypeId: number, childMapTypeId: number, days: number) {

    this.reportingService.getActivityReport(parentMapTypeId, childMapTypeId, days)
      .subscribe(
        result => {
          this.processActivityReportData(result);
        },
        error => {
          this.log.error('The report could not be run. Please try again.');
          this.reportComplete = true;
        }
      )
  }

  processActivityReportData(activityData: any[]) {
    this.activityReportData = activityData.map(ad => ({
      childUuid: ad.childUuid,
      parentUuid: ad.parentUuid,
      insertedAt: this.datePipe.transform(ad.insertedAt, 'yyyy-dd-MM HH:mm:SS')
    }));
  }

  filterReport(type: string) {
    switch (type) {
      case 'all': {
        this.filteredReportData = this.reportData;
        this.dsaPubTable.updateRows();
        break;
      }
      case 'activated': {
        this.filteredReportData = this.reportData.filter((org) => org.sharingActivated === 'Yes');
        this.dsaPubTable.updateRows();
        break;
      }
      case 'notActivated': {
        this.filteredReportData = this.reportData.filter((org) => org.sharingActivated != 'Yes');
        this.dsaPubTable.updateRows();
        break;
      }
      case 'error': {
        this.filteredReportData = this.reportData.filter((org) => org.inError);
        this.dsaPubTable.updateRows();
        break;
      }
      case 'ok': {
        this.filteredReportData = this.reportData.filter((org) => !org.inError);
        this.dsaPubTable.updateRows();
        break;
      }
      case 'notRecent': {
        this.filteredReportData = this.reportData.filter((org) => (new Date(org.lastReceived)) < this.compareDate);
        this.dsaPubTable.updateRows();
        break;
      }
      case 'recent': {
        this.filteredReportData = this.reportData.filter((org) => (new Date(org.lastReceived)) >= this.compareDate);
        this.dsaPubTable.updateRows();
        break;
      }
      default : {
        this.filteredReportData = this.reportData.filter((org) => org.systemSupplierType === type);
        this.dsaPubTable.updateRows();
        break;
      }
    }
  }
}

