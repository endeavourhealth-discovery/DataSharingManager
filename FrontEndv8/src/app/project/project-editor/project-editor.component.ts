import {Component, OnInit, ViewChild} from '@angular/core';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Project} from "src/app/project/models/Project";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from "@angular/router";
import {DatePipe} from "@angular/common";
import {MatDialog} from "@angular/material/dialog";
import {ProjectService} from "../project.service";
import {User} from "../models/User";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Organisation} from "../../organisation/models/Organisation";
import {Documentation} from "../../documentation/models/Documentation";
import {DocumentationComponent} from "../../documentation/documentation/documentation.component";
import {DocumentationService} from "../../documentation/documentation.service";
import {Cohort} from "src/app/cohort/models/Cohort";
import {OrganisationPickerComponent} from "../../organisation/organisation-picker/organisation-picker.component";
import {CohortPickerComponent} from "../../cohort/cohort-picker/cohort-picker.component";
import {DataSet} from "src/app/data-set/models/Dataset";
import {DataSetPickerComponent} from "src/app/data-set/data-set-picker/data-set-picker.component";
import {AuthorityToShare} from "../models/AuthorityToShare";
import { ApplicationPolicy } from '../models/ApplicationPolicy';
import {ProjectApplicationPolicy} from "../models/ProjectApplicationPolicy";
import {ExtractTechnicalDetails} from "../models/ExtractTechnicalDetails";
import {Schedule} from "../../scheduler/models/Schedule";
import {SchedulerComponent} from "../../scheduler/scheduler/scheduler.component";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {ValueSetsComponent} from "../../value-sets/value-sets/value-sets.component";

@Component({
  selector: 'app-project-editor',
  templateUrl: './project-editor.component.html',
  styleUrls: ['./project-editor.component.scss']
})
export class ProjectEditorComponent implements OnInit {

  @ViewChild('dsasTable', { static: false }) dsasTable: GenericTableComponent;
  @ViewChild('publishersTable', { static: false }) publishersTable: GenericTableComponent;
  @ViewChild('subscribersTable', { static: false }) subscribersTable: GenericTableComponent;
  @ViewChild('documentationsTable', { static: false }) documentationsTable: GenericTableComponent;
  @ViewChild('cohortsTable', { static: false }) cohortsTable: GenericTableComponent;
  @ViewChild('dataSetsTable', { static: false }) dataSetsTable: GenericTableComponent;
  @ViewChild('authToShareTable', { static: false }) authToShareTable: GenericTableComponent;
  @ViewChild('schedulesTable', { static: false }) schedulesTable: GenericTableComponent;

  project: Project;
  public activeProject: UserProject;
  private paramSubscriber: any;
  allowEdit = false;
  superUser = false;
  userId: string;
  disableStatus = false;
  userList: User[] = [];
  dsas: Dsa[] = [];
  dsasDetailsToShow = new Dsa().getDisplayItems();
  publishers: Organisation[] = [];
  publishersDetailsToShow = new Organisation().getDisplayItems();
  subscribers: Organisation[] = [];
  documentations: Documentation[] = [];
  documentationsDetailsToShow = new Documentation().getDisplayItems();
  cohorts: Cohort[] = [];
  cohortsDetailsToShow = new Cohort().getDisplayItems();
  dataSets: DataSet[] = [];
  dataSetsDetailsToShow = new DataSet().getDisplayItems();
  authToShare: AuthorityToShare[] = [];
  availablePolicies: ApplicationPolicy[];
  selectedApplicationPolicy: ApplicationPolicy;
  projectApplicationPolicy: ProjectApplicationPolicy;
  extractTechnicalDetails: ExtractTechnicalDetails = <ExtractTechnicalDetails>{};
  schedules: Schedule[] = [];
  schedulesDetailsToShow = new Schedule().getDisplayItems();

  businessCaseStatuses = [
    {num: 0, name: 'Submitted'},
    {num: 1, name: 'Approved'}
  ];

  storageProtocols = [
    {num: 0, name: 'Audit only'},
    {num: 1, name: 'Temporary Store And Forward'},
    {num: 2, name: 'Permanent Record Store'}
  ];

  consents = [
    {num: 0, name : 'Explicit Consent'},
    {num: 1, name : 'Implied Consent'}
  ];

  deidentificationLevel = [
    {num: 0, name: 'Patient identifiable data'},
    {num: 1, name: 'Pseudonymised data'}
  ];

  projectTypes = [
    {num: 0, name: 'Extract'},
    {num: 1, name: 'Query'}
  ];

  flowScheduleIds = [
    {num: 0, name: 'Daily'},
    {num: 1, name: 'On Demand'},
    {num: 2, name: 'Weekly'},
    {num: 3, name: 'Monthly'},
    {num: 4, name: 'Annually'},
    {num: 5, name: 'One off'},
    {num: 6, name: 'Quarterly'}
  ];

  outputFormat = [
    {num: 0, name: 'FHIR'},
    {num: 1, name: 'CSV'}
  ];

  securityInfrastructures = [
    {num: 0, name: 'N3'},
    {num: 1, name: 'PSN'},
    {num: 1, name: 'Internet'}
  ];

  securityArchitectures = [
    {num: 0, name: 'TLS/MA'},
    {num: 1, name: 'Secure FTP'}
  ];

  status = [
    {num: 0, name : 'Active'},
    {num: 1, name : 'Inactive'}
  ];


  constructor(private log: LoggerService,
              private projectService: ProjectService,
              private documentationService: DocumentationService,
              private router: Router,
              private route: ActivatedRoute,
              private datePipe: DatePipe,
              private userManagerService: UserManagerService,
              public dialog: MatDialog) {
  }

  ngOnInit() {
    this.userManagerService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      this.allowEdit = true;
      this.superUser = true;
      this.userId = null;
    } else if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
      this.superUser = false;
      this.userId = this.activeProject.userId;
    } else {
      this.allowEdit = false;
      this.superUser = false;
      this.userId = this.activeProject.userId;
    }
    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode'], params['id']);
      });
    this.getAvailableApplicationPolicies();
    this.getUserList();
  }

  protected performAction(action: string, itemUuid: string) {
    switch (action) {
      case 'add':
        this.create(itemUuid);
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  create(uuid: string) {
    this.project = {
      name : ''
    } as Project;
  }

  save(close: boolean) {

    // Populate Data Sharing Agreements before save
    this.project.dsas = {};
    for (let idx in this.dsas) {
      const dsa: Dsa = this.dsas[idx];
      this.project.dsas[dsa.uuid] = dsa.name;
    }

    // Populate publishers before save
    this.project.publishers = {};
    for (let idx in this.publishers) {
      const pub: Organisation = this.publishers[idx];
      this.project.publishers[pub.uuid] = pub.name;
    }

    // Populate subscribers before save
    this.project.subscribers = {};
    for (let idx in this.subscribers) {
      const sub: Organisation = this.subscribers[idx];
      this.project.subscribers[sub.uuid] = sub.name;
    }

    // Populate documents before save
    this.project.documentations = [];
    this.project.documentations = this.documentations;

    // Populate cohorts before save
    this.project.cohorts = {};
    for (let idx in this.cohorts) {
      const coh: Cohort = this.cohorts[idx];
      this.project.cohorts[coh.uuid] = coh.name;
    }

    // Populate dataSets before save
    this.project.dataSets = {};
    for (let idx in this.dataSets) {
      const ds: DataSet = this.dataSets[idx];
      this.project.dataSets[ds.uuid] = ds.name;
    }

    // Populate extract technical details before save
    this.project.extractTechnicalDetails = null;
    this.project.extractTechnicalDetails = this.extractTechnicalDetails;

    // Populate schedule before save
    if (this.schedules[0]) {
      this.project.schedule = this.schedules[0];
      this.project.schedules = {};
      if (this.project.schedule.uuid) {
        this.project.schedules[this.project.schedule.uuid] = this.project.schedule.cronDescription;
      }
    }
    this.projectService.saveProject(this.project)
      .subscribe(saved => {
          this.project.uuid = saved;
          this.saveApplicationPolicy();
          this.log.success('Project saved');
          if (close) {
            window.history.back();
          }
        },
        error => this.log.error('The project could not be saved. Please try again.')
      );
  }

  close() {
    window.history.back();
  }

  saveApplicationPolicy() {
    this.projectService.saveProjectApplicationPolicy(this.projectApplicationPolicy)
      .subscribe(
        (response) => {
        },
        (error) => this.log.error('Project application policy could not be saved. Please try again.')
      );
  }

  load(uuid: string) {
    this.projectService.getProject(uuid)
      .subscribe(result =>  {
          this.project = result;
          this.project.startDate = this.datePipe.transform(this.project.startDate,"yyyy-MM-dd");
          this.project.endDate = this.datePipe.transform(this.project.endDate,"yyyy-MM-dd");
          this.checkEndDate();
          this.getDsas();
          this.getPublishers();
          this.getSubscribers();
          this.getDocumentations();
          this.getCohorts();
          this.getDataSets();
          this.getAuthToShare();
          this.getAvailableApplicationPolicies();
          this.getProjectApplicationPolicy();
          this.getAssociatedExtractTechnicalDetails();
          this.getSchedule();
        },
        error => this.log.error('The data processing agreement could not be loaded. Please try again.')
      );
  }

  getUserList() {
    const vm = this;
    vm.projectService.getUsers()
      .subscribe(
        (result) => vm.userList = result,
        (error) => this.log.error('User list could not be loaded. Please try again.')
      );
  }

  checkEndDate() {
    if (this.project.endDate === null) {
      this.disableStatus = false;
      return;
    }

    let today = new Date();
    today.setHours(0,0,0,0);
    let endDate = new Date(this.project.endDate);

    if (endDate < today) {
      this.project.projectStatusId = 1;
      this.disableStatus = true;
    } else {
      this.disableStatus = false;
    }
  }

  getDsas() {
    this.projectService.getLinkedDsas(this.project.uuid)
      .subscribe(
        result => {
          this.dsas = result;
        },
        error => this.log.error('The associated data sharing agreements could not be loaded. Please try again.')
      );
  }

  dsaClicked(item: Dsa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  deleteDsas() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove DSA', 'Are you sure you want to remove DSA(s)?',
      'Remove DSA', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dsasTable.selection.selected.length; i++) {
              let dsa = this.dsasTable.selection.selected[i];
              this.dsas.forEach( (item, index) => {
                if(item === dsa) this.dsas.splice(index,1);
              });
            }
            this.dsasTable.updateRows();
            this.log.success('Remove successful.');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addDsas() {
    const dialogRef = this.dialog.open(DataSharingAgreementPickerComponent, {
      width: '800px',
    })
    dialogRef.afterClosed().subscribe(result => {
      for (let dsa of result) {
        if (!this.dsas.some(x => x.uuid === dsa.uuid)) {
          this.dsas.push(dsa);
          this.dsasTable.updateRows();
        }
      }
    })
    // const dialogRef = this.dialog.open(ValueSetsComponent, {
    //   width: '1000px',
    // })
    // dialogRef.afterClosed().subscribe(result => {
    //   for (let vs of result) {
    //     console.log(vs);
    //   }
    // })
  }

  getPublishers() {
    this.projectService.getLinkedPublishers(this.project.uuid)
      .subscribe(
        result => this.publishers = result,
        error => this.log.error('The associated publishers could not be loaded. Please try again.')
      );
  }

  publisherClicked(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  deletePublishers() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove publisher', 'Are you sure you want to remove publisher(s)?',
      'Remove publisher', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.publishersTable.selection.selected.length; i++) {
              let publisher = this.publishersTable.selection.selected[i];
              this.publishers.forEach( (item, index) => {
                if(item === publisher) this.publishers.splice(index,1);
              });
            }
            this.publishersTable.updateRows();
            this.log.success('Remove successful');
          } else {
            this.log.success('Remove cancelled')
          }
        });
  }

  addPublishers() {
    if (!this.dsas[0]) {
      this.log.error('The project must be associated with a data sharing agreement before editing publishers.');
    } else {
      const dialogRef = this.dialog.open(OrganisationPickerComponent, {
        width: '800px',
        data: { searchType: 'publisher', uuid: '', regionUUID: '', dsaUUID: this.dsas[0].uuid }
      })
      dialogRef.afterClosed().subscribe(result => {
        for (let org of result) {
          if (!this.publishers.some(x => x.uuid === org.uuid)) {
            this.publishers.push(org);
            this.publishersTable.updateRows();
          }
        }
      })
    }
  }

  getSubscribers() {
    this.projectService.getLinkedSubscribers(this.project.uuid)
      .subscribe(
        result => this.subscribers = result,
        error => this.log.error('The associated publishers could not be loaded. Please try again.')
      );
  }

  deleteSubscribers() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove subscriber', 'Are you sure you want to remove subscriber(s)?',
      'Remove subscriber', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.subscribersTable.selection.selected.length; i++) {
              let subscriber = this.subscribersTable.selection.selected[i];
              this.subscribers.forEach( (item, index) => {
                if(item === subscriber) this.subscribers.splice(index,1);
              });
            }
            this.subscribersTable.updateRows();
            this.log.success('Remove successful.');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addSubscribers() {
    if (!this.dsas[0]) {
      this.log.error('The project must be associated with a data sharing agreement before editing subscribers.');
    } else {
      const dialogRef = this.dialog.open(OrganisationPickerComponent, {
        width: '800px',
        data: { searchType: 'subscriber', uuid: '', regionUUID: '', dsaUUID: this.dsas[0].uuid }
      })
      dialogRef.afterClosed().subscribe(result => {
        for (let org of result) {
          if (!this.subscribers.some(x => x.uuid === org.uuid)) {
            this.subscribers.push(org);
            this.subscribersTable.updateRows();
          }
        }
      })
    }
  }

  getDocumentations() {
    this.documentationService.getAllAssociatedDocuments(this.project.uuid, '14')
      .subscribe(
        result => this.documentations = result,
        error => this.log.error('The associated documentations could not be loaded. Please try again.')
      );
  }

  deleteDocumentations() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete document', 'Are you sure you want to delete document(s)?',
      'Delete document', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.documentationsTable.selection.selected.length; i++) {
              let document = this.documentationsTable.selection.selected[i];
              this.documentations.forEach( (item, index) => {
                if(item === document) this.documentations.splice(index,1);
              });
            }
            this.documentationsTable.updateRows();
            this.log.success('Delete successful.');
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  addDocumentation() {
    const dialogRef = this.dialog.open(DocumentationComponent, {
      width: '550px',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.documentations.push(result);
        this.documentationsTable.updateRows();
      }
    });
  }

  getCohorts() {
    this.projectService.getLinkedBasePopulation(this.project.uuid)
      .subscribe(
        result => this.cohorts = result,
        error => this.log.error('The associated cohort could not be loaded. Please try again.')
      );
  }

  cohortClicked(item: Cohort) {
    this.router.navigate(['/cohort', item.uuid, 'edit']);
  }

  deleteCohorts() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove cohort', 'Are you sure you want to remove cohort(s)?',
      'Remove cohort', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.cohortsTable.selection.selected.length; i++) {
              let cohort = this.cohortsTable.selection.selected[i];
              this.cohorts.forEach( (item, index) => {
                if(item === cohort) this.cohorts.splice(index,1);
              });
            }
            this.cohortsTable.updateRows();
            this.log.success('Remove successful.');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addCohorts() {
    const dialogRef = this.dialog.open(CohortPickerComponent, {
      width: '1200px',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        for (let cohort of result) {
          if (!this.cohorts.some(x => x.uuid === cohort.uuid)) {
            this.cohorts.push(cohort);
            this.cohortsTable.updateRows();
          }
        }
      }
    });
  }

  getDataSets() {
    this.projectService.getLinkedDataSets(this.project.uuid)
      .subscribe(
        result => this.dataSets = result,
        error => this.log.error('The associated data sets could not be loaded. Please try again.')
      );
  }

  dataSetClicked(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
  }

  deleteDataSets() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove data set', 'Are you sure you want to remove data set(s)?',
      'Remove data set', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dataSetsTable.selection.selected.length; i++) {
              let dataset = this.dataSetsTable.selection.selected[i];
              this.dataSets.forEach( (item, index) => {
                if(item === dataset) this.dataSets.splice(index,1);
              });
            }
            this.dataSetsTable.updateRows();
            this.log.success('Remove successful.');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addDataSets() {
    const dialogRef = this.dialog.open(DataSetPickerComponent, {
      width: '1200px',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        for (let dataSet of result) {
          if (!this.dataSets.some(x => x.uuid === dataSet.uuid)) {
            this.dataSets.push(dataSet);
            this.dataSetsTable.updateRows();
          }
        }
      }
    });
  }

  getAuthToShare() {
    this.projectService.getUsersAssignedToProject(this.project.uuid)
      .subscribe(
        result => this.authToShare = result,
        error => this.log.error('Authority to share could not be loaded. Please try again.')
      );
  }

  getAvailableApplicationPolicies() {
    this.projectService.getAvailableProjectApplicationPolicy()
      .subscribe(
        (result) => this.availablePolicies = result,
        (error) => this.log.error('Available application policies could not be loaded. Please try again.')
      );
  }

  getProjectApplicationPolicy() {
    this.projectService.getProjectApplicationPolicy(this.project.uuid)
      .subscribe(
        (result) => {
          this.projectApplicationPolicy = result;
          this.selectedApplicationPolicy = this.availablePolicies.find(r => {
            return r.id === this.projectApplicationPolicy.applicationPolicyId;
          });
        },
        (error) => {
          this.log.error('Project application policy could not be loaded. Please try again.');
        }
      );
  }

  changeUserApplicationPolicy(policyId: string) {
    let changedPolicy = new ProjectApplicationPolicy();
    changedPolicy.projectUuid = this.project.uuid;
    changedPolicy.applicationPolicyId = policyId;
    this.projectApplicationPolicy = changedPolicy;
  }

  getAssociatedExtractTechnicalDetails() {
    this.projectService.getAssociatedExtractTechDetails(this.project.uuid)
      .subscribe(
      result => this.extractTechnicalDetails = result
    );
  }

  uploadExtraTechDetails(whichFile: number) {
    const dialogRef = this.dialog.open(DocumentationComponent, {
      width: '550px',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (whichFile == 1) {
          this.log.success('Uploading SFTP host public key file complete.');
          this.extractTechnicalDetails.sftpHostPublicKeyFilename = result.filename;
          this.extractTechnicalDetails.sftpHostPublicKeyFileData = result.fileData;
        } else if (whichFile == 2) {
          this.log.success('Uploading SFTP client private key file complete.');
          this.extractTechnicalDetails.sftpClientPrivateKeyFilename = result.filename;
          this.extractTechnicalDetails.sftpClientPrivateKeyFileData = result.fileData;
        } else if (whichFile == 3) {
          this.log.success('Uploading customer public key file complete.');
          this.extractTechnicalDetails.pgpCustomerPublicKeyFilename = result.filename;
          this.extractTechnicalDetails.pgpCustomerPublicKeyFileData = result.fileData;
        } else if (whichFile == 4) {
          this.log.success('Uploading internal public key file complete.');
          this.extractTechnicalDetails.pgpInternalPublicKeyFilename = result.filename;
          this.extractTechnicalDetails.pgpInternalPublicKeyFileData = result.fileData;
        }
      }
    });
  }

  clearExtraTechDetails(whichFile: number) {
    if (whichFile == 1) {
      this.extractTechnicalDetails.sftpHostPublicKeyFilename = null;
      this.extractTechnicalDetails.sftpHostPublicKeyFileData = null;
    } else if (whichFile == 2) {
      this.extractTechnicalDetails.sftpClientPrivateKeyFilename = null;
      this.extractTechnicalDetails.sftpClientPrivateKeyFileData = null;
    } else if (whichFile == 3) {
      this.extractTechnicalDetails.pgpCustomerPublicKeyFilename = null;
      this.extractTechnicalDetails.pgpCustomerPublicKeyFileData = null;
    } else if (whichFile == 4) {
      this.extractTechnicalDetails.pgpInternalPublicKeyFilename = null;
      this.extractTechnicalDetails.pgpInternalPublicKeyFileData = null;
    }
  }

  getSchedule() {
    this.projectService.getLinkedSchedule(this.project.uuid)
      .subscribe(
        result => {
          if (result) {
            this.schedules = new Array<Schedule>();
            this.schedules[0] = result;
            this.schedulesTable.updateRows();
          } else {
            this.schedules = new Array<Schedule>();
          }
        }
      );
  }

  scheduleClicked(item: Schedule) {
    let index = this.schedules.indexOf(item);
    const dialogRef = this.dialog.open(SchedulerComponent, {
      width: '1200px',
      data: {schedule: item, allowTime: true},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.schedules[index] = result;
        this.schedulesTable.updateRows();
      }
    });
  }

  deleteSchedules() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete schedule', 'Are you sure you want to delete schedule(s)?',
      'Delete schedule', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.schedulesTable.selection.selected.length; i++) {
              let schedule = this.schedulesTable.selection.selected[i];
              this.schedules.forEach( (item, index) => {
                if(item === schedule) this.schedules.splice(index,1);
              });
            }
            this.schedulesTable.updateRows();
            this.log.success('Delete successful.');
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  addSchedule() {
    if (this.schedules.length == 0) {
      const dialogRef = this.dialog.open(SchedulerComponent, {
        width: '1200px',
        data: {schedule: null, allowTime: true},
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.schedules.push(result);
          this.schedulesTable.updateRows();
        }
      });
    } else {
      this.log.error('Cannot add multiple schedules.');
    }
  }
}
