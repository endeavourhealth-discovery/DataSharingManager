import {Component, OnInit, ViewChild} from '@angular/core';
import {Project} from "src/app/project/models/Project";
import {
  GenericTableComponent,
  ItemLinkageService,
  LoggerService,
  MessageBoxDialogComponent,
  UserManagerService
} from "dds-angular8";
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
import {ApplicationPolicy} from '../models/ApplicationPolicy';
import {ProjectApplicationPolicy} from "../models/ProjectApplicationPolicy";
import {ExtractTechnicalDetails} from "../models/ExtractTechnicalDetails";
import {Schedule} from "../../scheduler/models/Schedule";
import {SchedulerComponent} from "../../scheduler/scheduler/scheduler.component";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {ProjectDialogComponent} from "../project-dialog/project-dialog.component";
import {ExtractDetailsDialogComponent} from "../extract-details-dialog/extract-details-dialog.component";
import {UserProject} from "dds-angular8/user-manager";

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
  @ViewChild('extractTechnicalDetailsTable', { static: false }) extractTechnicalDetailsTable: GenericTableComponent;
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
  extractTechnicalDetails: ExtractTechnicalDetails[] = [];
  extractTechnicalDetailToShow = new ExtractTechnicalDetails().getDisplayItems();
  schedules: Schedule[] = [];
  schedulesDetailsToShow = new Schedule().getDisplayItems();

  businessCaseStatuses = this.linkageService.businessCaseStatuses;

  storageProtocols = this.linkageService.storageProtocols;

  consents = this.linkageService.consents;

  deidentificationLevel = this.linkageService.deidentificationLevel;

  projectTypes = this.linkageService.projectTypes;

  flowScheduleIds = this.linkageService.flowScheduleIds;

  outputFormat = this.linkageService.outputFormat;

  securityInfrastructures = this.linkageService.securityInfrastructures;

  securityArchitectures = this.linkageService.securityArchitectures;

  status = this.linkageService.status;

  businessCaseStatus: string;
  leadUser: string;
  technicalLeadUser: string;
  storageProtocolId: string;
  consentModelId: string;
  deidentificationLevelValue: string;
  projectTypeId: string;
  flowScheduleId: string;
  outputFormatValue: string;
  selectedApplicationPolicyValue: string;
  securityInfrastructureId: string;
  securityArchitectureId: string;
  projectStatusId: string;

  constructor(private log: LoggerService,
              private projectService: ProjectService,
              private documentationService: DocumentationService,
              private router: Router,
              private route: ActivatedRoute,
              private datePipe: DatePipe,
              private userManagerService: UserManagerService,
              public dialog: MatDialog,
              private linkageService: ItemLinkageService) {
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

  editProject() {
    const dialogRef = this.dialog.open(ProjectDialogComponent, {
      data: {mode: 'edit', uuid: this.project.uuid, project: this.project},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.project = result;
        this.log.success('Project saved');
        this.updateLinkValues();
      }
    });
  }

  updateLinkValues() {
    this.leadUser = '';
    this.technicalLeadUser = '';
    this.getUserList();
    this.businessCaseStatus = '';
    if (this.project.businessCaseStatus != null) {
      this.businessCaseStatus = this.businessCaseStatuses[this.project.businessCaseStatus].name;
    }
    this.storageProtocolId = '';
    if (this.project.storageProtocolId != null) {
      this.storageProtocolId = this.storageProtocols[this.project.storageProtocolId].name;
    }
    this.consentModelId = '';
    if (this.project.consentModelId != null) {
      this.consentModelId = this.consents[this.project.consentModelId].name;
    }
    this.deidentificationLevelValue = '';
    if (this.project.deidentificationLevel != null) {
      this.deidentificationLevelValue = this.deidentificationLevel[this.project.deidentificationLevel].name;
    }
    this.projectTypeId = '';
    if (this.project.projectTypeId != null) {
      this.projectTypeId = this.projectTypes[this.project.projectTypeId].name;
    }
    this.flowScheduleId = '';
    if (this.project.flowScheduleId != null) {
      this.flowScheduleId = this.flowScheduleIds[this.project.flowScheduleId].name;
    }
    this.outputFormatValue = '';
    if (this.project.outputFormat != null) {
      this.outputFormatValue = this.outputFormat[this.project.outputFormat].name;
    }
    this.selectedApplicationPolicyValue = '';
    this.getProjectApplicationPolicy();
    this.securityInfrastructureId = '';
    if (this.project.securityInfrastructureId != null) {
      this.securityInfrastructureId = this.securityInfrastructures[this.project.securityInfrastructureId].name;
    }
    this.securityArchitectureId = '';
    if (this.project.securityArchitectureId != null) {
      this.securityArchitectureId = this.securityArchitectures[this.project.securityArchitectureId].name;
    }
    this.projectStatusId = '';
    if (this.project.projectStatusId != null) {
      this.projectStatusId = this.status[this.project.projectStatusId].name;
    }
  }

  updateMappings(type: string) {
    this.projectService.updateMappings(this.project)
      .subscribe(saved => {
          this.project.uuid = saved;
            this.log.success(type + ' updated successfully.');
            this.refreshMappings(type);
        },
        error => {
          this.log.error('The project could not be saved. Please try again.');
          this.refreshMappings(type);
        }
      );
  }

  refreshMappings(type: string) {
    if (type == 'Sharing agreements') {
      this.getDsas();
    } else if (type == 'Publishers') {
      this.getPublishers();
    } else if (type == 'Subscribers') {
      this.getSubscribers();
    } else if (type == 'Documents') {
      this.getDocumentations();
    } else if (type == 'Cohorts') {
      this.getCohorts();
    } else if (type == 'Data sets') {
      this.getDataSets();
    } else if (type == 'Extract technical details') {
      this.getAssociatedExtractTechnicalDetails();
    } else if (type == 'Schedule') {
      this.getSchedule();
    }
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
          this.getAssociatedExtractTechnicalDetails();
          this.getSchedule();
          this.updateLinkValues();
        },
        error => this.log.error('The project could not be loaded. Please try again.')
      );
  }

  getUserList() {
    this.projectService.getUsers()
      .subscribe(
        (result) => {
          this.userList = result;
          for (let user of this.userList) {
            if (user.uuid == this.project.leadUser) {
              this.leadUser = user.forename + ' ' + user.surname + ' (' + user.email + ')';
            } else if (user.uuid == this.project.technicalLeadUser) {
              this.technicalLeadUser = user.forename + ' ' + user.surname + ' (' + user.email + ')';
            }
          }
        },
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
          this.dsasTable.updateRows();
        },
        error => this.log.error('The associated data sharing agreements could not be loaded. Please try again.')
      );
  }

  dsaClicked(item: Dsa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  deleteDsas() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove sharing agreements', 'Are you sure you want to remove sharing agreements?',
      'Remove sharing agreements', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dsasTable.selection.selected.length; i++) {
              let dsa = this.dsasTable.selection.selected[i];
              this.dsas.forEach( (item, index) => {
                if(item === dsa) this.dsas.splice(index,1);
              });
            }
            this.clearMappings();
            this.project.dsas = {};
            for (const idx in this.dsas) {
              const dsa: Dsa = this.dsas[idx];
              this.project.dsas[dsa.uuid] = dsa.name;
            }
            this.updateMappings('Sharing agreements');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addDsas() {
    const dialogRef = this.dialog.open(DataSharingAgreementPickerComponent, {
      minWidth: '50vw',
      data: { allowMultiple: false, existing: this.dsas }
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dsa of result) {
        if (!this.dsas.some(x => x.uuid === dsa.uuid)) {
          this.dsas.push(dsa);
        }
      }
      this.clearMappings();
      this.project.dsas = {};
      for (const idx in this.dsas) {
        const dsa: Dsa = this.dsas[idx];
        this.project.dsas[dsa.uuid] = dsa.name;
      }
      this.updateMappings('Sharing agreements');
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
        result => {
          this.publishers = result;
          this.publishersTable.updateRows();
        },
        error => this.log.error('The associated publishers could not be loaded. Please try again.')
      );
  }

  publisherClicked(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  deletePublishers() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove publishers', 'Are you sure you want to remove publishers?',
      'Remove publishers', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.publishersTable.selection.selected.length; i++) {
              let publisher = this.publishersTable.selection.selected[i];
              this.publishers.forEach( (item, index) => {
                if(item === publisher) this.publishers.splice(index,1);
              });
            }
            this.clearMappings();
            this.project.publishers = {};
            for (const idx in this.publishers) {
              const org: Organisation = this.publishers[idx];
              this.project.publishers[org.uuid] = org.name;
            }
            this.updateMappings('Publishers');
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
        minWidth: '50vw',
        data: { searchType: 'publisher', uuid: '', regionUUID: '', dsaUUID: this.dsas[0].uuid, existingOrgs: this.publishers }
      })
      dialogRef.afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        for (let org of result) {
          if (!this.publishers.some(x => x.uuid === org.uuid)) {
            this.publishers.push(org);
          }
        }
        this.clearMappings();
        this.project.publishers = {};
        for (const idx in this.publishers) {
          const org: Organisation = this.publishers[idx];
          this.project.publishers[org.uuid] = org.name;
        }
        this.updateMappings('Publishers');
      })
    }
  }

  getSubscribers() {
    this.projectService.getLinkedSubscribers(this.project.uuid)
      .subscribe(
        result => {
          this.subscribers = result;
          this.subscribersTable.updateRows();
        },
        error => this.log.error('The associated publishers could not be loaded. Please try again.')
      );
  }

  deleteSubscribers() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove subscribers', 'Are you sure you want to remove subscribers?',
      'Remove subscribers', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.subscribersTable.selection.selected.length; i++) {
              let subscriber = this.subscribersTable.selection.selected[i];
              this.subscribers.forEach( (item, index) => {
                if(item === subscriber) this.subscribers.splice(index,1);
              });
            }
            this.clearMappings();
            this.project.subscribers = {};
            for (const idx in this.subscribers) {
              const org: Organisation = this.subscribers[idx];
              this.project.subscribers[org.uuid] = org.name;
            }
            this.updateMappings('Subscribers');
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
        minWidth: '50vw',
        data: { searchType: 'subscriber', uuid: '', regionUUID: '', dsaUUID: this.dsas[0].uuid, existingOrgs: this.subscribers }
      })
      dialogRef.afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        for (let org of result) {
          if (!this.subscribers.some(x => x.uuid === org.uuid)) {
            this.subscribers.push(org);
          }
        }
        this.clearMappings();
        this.project.subscribers = {};
        for (const idx in this.subscribers) {
          const org: Organisation = this.subscribers[idx];
          this.project.subscribers[org.uuid] = org.name;
        }
        this.updateMappings('Subscribers');
      })
    }
  }

  documentationClicked(item: Documentation) {
    const element = document.createElement('a');
    element.href = item.fileData;
    element.download = item.filename;
    document.body.appendChild(element);
    element.click();
  }

  getDocumentations() {
    this.documentationService.getAllAssociatedDocuments(this.project.uuid, '14')
      .subscribe(
        result => {
          this.documentations = result;
          this.documentationsTable.updateRows();
        },
        error => this.log.error('The associated documentations could not be loaded. Please try again.')
      );
  }

  deleteDocumentations() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete documents', 'Are you sure you want to delete documents?',
      'Delete documents', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.documentationsTable.selection.selected.length; i++) {
              let document = this.documentationsTable.selection.selected[i];
              this.documentations.forEach( (item, index) => {
                if(item === document) this.documentations.splice(index,1);
              });
            }
            this.clearMappings();
            this.project.documentations = [];
            this.project.documentations = this.documentations;
            this.updateMappings('Documents');
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  addDocumentation() {
    const dialogRef = this.dialog.open(DocumentationComponent, {
      width: '30vw',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.projectService.addDocument(this.project.uuid, result)
          .subscribe(saved => {
              this.project.uuid = saved;
              this.log.success('Documents updated successfully.');
              this.getDocumentations();
            },
            error => {
              this.log.error('The project could not be saved. Please try again.');
              this.getDocumentations();
            }
          );
      }
    });
  }

  getCohorts() {
    this.projectService.getLinkedBasePopulation(this.project.uuid)
      .subscribe(
        result => {
          this.cohorts = result;
          this.cohortsTable.updateRows();
        },
        error => this.log.error('The associated cohort could not be loaded. Please try again.')
      );
  }

  cohortClicked(item: Cohort) {
    this.router.navigate(['/cohort', item.uuid, 'edit']);
  }

  deleteCohorts() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove cohorts', 'Are you sure you want to remove cohorts?',
      'Remove cohorts', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.cohortsTable.selection.selected.length; i++) {
              let cohort = this.cohortsTable.selection.selected[i];
              this.cohorts.forEach( (item, index) => {
                if(item === cohort) this.cohorts.splice(index,1);
              });
            }
            this.clearMappings();
            this.project.cohorts = {};
            for (const idx in this.cohorts) {
              const cohort: Cohort = this.cohorts[idx];
              this.project.cohorts[cohort.uuid] = cohort.name;
            }
            this.updateMappings('Cohorts');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addCohorts() {
    const dialogRef = this.dialog.open(CohortPickerComponent, {
      minWidth: '50vw',
      data: {userId: this.activeProject.userId, existing: this.cohorts}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        for (let cohort of result) {
          if (!this.cohorts.some(x => x.uuid === cohort.uuid)) {
            this.cohorts.push(cohort);
          }
        }
        this.clearMappings();
        this.project.cohorts = {};
        for (const idx in this.cohorts) {
          const cohort: Cohort = this.cohorts[idx];
          this.project.cohorts[cohort.uuid] = cohort.name;
        }
        this.updateMappings('Cohorts');
      }
    });
  }

  getDataSets() {
    this.projectService.getLinkedDataSets(this.project.uuid)
      .subscribe(
        result => {
          this.dataSets = result;
          this.dataSetsTable.updateRows();
        },
        error => this.log.error('The associated data sets could not be loaded. Please try again.')
      );
  }

  dataSetClicked(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
  }

  deleteDataSets() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove data sets', 'Are you sure you want to remove data sets?',
      'Remove data sets', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dataSetsTable.selection.selected.length; i++) {
              let dataset = this.dataSetsTable.selection.selected[i];
              this.dataSets.forEach( (item, index) => {
                if(item === dataset) this.dataSets.splice(index,1);
              });
            }
            this.clearMappings();
            this.project.dataSets = {};
            for (const idx in this.dataSets) {
              const dataSet: DataSet = this.dataSets[idx];
              this.project.dataSets[dataSet.uuid] = dataSet.name;
            }
            this.updateMappings('Data sets');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addDataSets() {
    const dialogRef = this.dialog.open(DataSetPickerComponent, {
      minWidth: '50vw',
      data: {userId: this.activeProject.userId, existing: this.dataSets}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        for (let dataSet of result) {
          if (!this.dataSets.some(x => x.uuid === dataSet.uuid)) {
            this.dataSets.push(dataSet);
          }
        }
        this.clearMappings();
        this.project.dataSets = {};
        for (const idx in this.dataSets) {
          const dataSet: DataSet = this.dataSets[idx];
          this.project.dataSets[dataSet.uuid] = dataSet.name;
        }
        this.updateMappings('Data sets');
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

  getProjectApplicationPolicy() {
    this.projectService.getAvailableProjectApplicationPolicy()
      .subscribe(
        (result) => {
          this.availablePolicies = result;
          this.projectService.getProjectApplicationPolicy(this.project.uuid)
            .subscribe(
              (result) => {
                this.projectApplicationPolicy = result;
                this.selectedApplicationPolicy = this.availablePolicies.find(r => {
                  return r.id === this.projectApplicationPolicy.applicationPolicyId;
                });
                this.changeUserApplicationPolicy(this.selectedApplicationPolicy.id);
                this.selectedApplicationPolicyValue = this.selectedApplicationPolicy.name;
              },
              (error) => {
                this.log.error('Project application policy could not be loaded. Please try again.');
              }
            );
        },
        (error) => this.log.error('Available application policies could not be loaded. Please try again.')
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
      result => {
        if (result) {
          this.extractTechnicalDetails = new Array<ExtractTechnicalDetails>();
          this.extractTechnicalDetails.push(result);
        } else {
          this.extractTechnicalDetails = new Array<ExtractTechnicalDetails>();
        }
        this.extractTechnicalDetailsTable.updateRows();
      }
    );
  }

  extractTechnicalDetailClicked(item: ExtractTechnicalDetails) {
    let index = this.extractTechnicalDetails.indexOf(item);
    const dialogRef = this.dialog.open(ExtractDetailsDialogComponent, {
      data: {extractTechnicalDetail: item},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.extractTechnicalDetails[index] = result;
        if (this.extractTechnicalDetails[0]) {
          this.clearMappings();
          this.project.extractTechnicalDetails = this.extractTechnicalDetails[0];
          this.updateMappings("Extract technical details");
        }
      }
    });
  }

  deleteExtractTechnicalDetail() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete Extract technical details', 'Are you sure you want to delete extract technical details?',
      'Delete Extract technical details', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.extractTechnicalDetailsTable.selection.selected.length; i++) {
              let details = this.extractTechnicalDetailsTable.selection.selected[i];
              this.extractTechnicalDetails.forEach( (item, index) => {
                if(item === details) this.extractTechnicalDetails.splice(index,1);
              });
            }
            this.clearMappings();
            this.project.extractTechnicalDetails = null;
            this.updateMappings("Extract technical details");
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  addExtractTechnicalDetail() {
    if (!this.extractTechnicalDetails[0]) {
      const dialogRef = this.dialog.open(ExtractDetailsDialogComponent, {
        data: {extractTechnicalDetail: null},
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.extractTechnicalDetails.push(result);
          if (this.extractTechnicalDetails[0]) {
            this.project.extractTechnicalDetails = this.extractTechnicalDetails[0];
            this.clearMappings();
            this.updateMappings("Extract technical details");
          }
        }
      });
    } else {
      this.log.error('Cannot add multiple extract technical details.');
    }
  }

  getSchedule() {
    this.projectService.getLinkedSchedule(this.project.uuid)
      .subscribe(
        result => {
          if (result) {
            this.schedules = new Array<Schedule>();
            this.schedules[0] = result;
          } else {
            this.schedules = new Array<Schedule>();
          }
          this.schedulesTable.updateRows();
        }
      );
  }

  scheduleClicked(item: Schedule) {
    let index = this.schedules.indexOf(item);
    const dialogRef = this.dialog.open(SchedulerComponent, {
      data: {schedule: item, allowTime: true},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.schedules[index] = result;
        if (this.schedules[0]) {
          this.project.schedule = this.schedules[0];
          this.clearMappings();
          this.project.schedules = {};
          if (this.project.schedule.uuid) {
            this.project.schedules[this.project.schedule.uuid] = this.project.schedule.cronDescription;
            this.updateMappings("Schedule");
          }
        }
      }
    });
  }

  deleteSchedules() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete schedule', 'Are you sure you want to delete schedule?',
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
            this.clearMappings();
            this.project.schedules = {};
            this.project.schedule = null;
            this.updateMappings("Schedule");
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  addSchedule() {
    if (this.schedules.length == 0) {
      const dialogRef = this.dialog.open(SchedulerComponent, {
        data: {schedule: null, allowTime: true},
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.schedules.push(result);
          if (this.schedules[0]) {
            this.project.schedule = this.schedules[0];
            this.clearMappings();
            this.project.schedules = {};
            if (this.project.schedule.uuid) {
              this.project.schedules[this.project.schedule.uuid] = this.project.schedule.cronDescription;
              this.updateMappings("Schedule");
            }
          }
        }
      });
    } else {
      this.log.error('Cannot add multiple schedules.');
    }
  }

  clearMappings() {
    this.project.dsas = null;
    this.project.publishers = null;
    this.project.subscribers = null;
    this.project.documentations = null;
    this.project.cohorts = null;
    this.project.dataSets = null;
    this.project.documentations = null
    this.project.schedule = this.schedules[0];
    this.project.extractTechnicalDetails = this.extractTechnicalDetails[0];
  }
}
