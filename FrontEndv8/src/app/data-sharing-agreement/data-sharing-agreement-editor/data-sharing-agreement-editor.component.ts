import {Component, OnInit, ViewChild} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute, Router} from '@angular/router';
import {
  GenericTableComponent,
  ItemLinkageService,
  LoggerService,
  MessageBoxDialogComponent,
  UserManagerService
} from "dds-angular8";
import {Dsa} from '../models/Dsa';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
import {Purpose} from '../../models/Purpose';
import {PurposeComponent} from "../../purpose/purpose/purpose.component";
import {Region} from '../../region/models/Region';
import {Marker} from '../../region/models/Marker';
import {RegionPickerComponent} from '../../region/region-picker/region-picker.component';
import {Project} from "../../project/models/Project";
import {ProjectPickerComponent} from "../../project/project-picker/project-picker.component";
import {Organisation} from '../../organisation/models/Organisation';
import {OrganisationPickerComponent} from '../../organisation/organisation-picker/organisation-picker.component';
import {Documentation} from "../../documentation/models/Documentation";
import {DocumentationService} from "../../documentation/documentation.service";
import {DocumentationComponent} from "../../documentation/documentation/documentation.component";
import {DataSharingAgreementDialogComponent} from "src/app/data-sharing-agreement/data-sharing-agreement-dialog/data-sharing-agreement-dialog.component";
import {GoogleMapsDialogComponent} from "../../google-maps-viewer/google-maps-dialog/google-maps-dialog.component";
import {Cohort} from "../../cohort/models/Cohort";
import {DataSet} from "../../data-set/models/Dataset";
import {CohortPickerComponent} from "../../cohort/cohort-picker/cohort-picker.component";
import {DataSetPickerComponent} from "../../data-set/data-set-picker/data-set-picker.component";
import {UserProject} from "dds-angular8/user-manager";

@Component({
  selector: 'app-data-sharing-agreement-editor',
  templateUrl: './data-sharing-agreement-editor.component.html',
  styleUrls: ['./data-sharing-agreement-editor.component.css']
})

export class DataSharingAgreementEditorComponent implements OnInit {
  private paramSubscriber: any;

  dsa: Dsa = <Dsa>{};
  purposes: Purpose[] = [];
  benefits: Purpose[] = [];
  regions: Region[] = [];
  projects: Project[] = [];
  publishers: Organisation[] = [];
  subscribers: Organisation[] = [];
  documentations: Documentation[] = [];
  cohorts: Cohort[] = [];
  dataSets: DataSet[] = [];

  publisherMarkers: Marker[];
  subscriberMarkers: Marker[];
  mapMarkers: Marker[] = [];
  showPub = true;
  allowEdit = false;
  disableStatus = false;
  superUser = false;
  userId: string;

  model = 1;

  public activeProject: UserProject;


  statuses = this.linkageService.status;

  consents = this.linkageService.consents;

  purposeDetailsToShow = new Purpose().getDisplayItems();
  benefitDetailsToShow = new Purpose().getDisplayItems();
  regionDetailsToShow = new Region().getDisplayItems();
  projectDetailsToShow = new Project().getDisplayItems();
  publisherDetailsToShow = new Organisation().getDisplayItems();
  subscriberDetailsToShow = new Organisation().getDisplayItems();
  documentDetailsToShow = new Documentation().getDisplayItems();
  cohortDetailsToShow = new Cohort().getDisplayItems();
  dataSetDetailsToShow = new DataSet().getDisplayItems();

  @ViewChild('purposesTable', {static: false}) purposesTable: GenericTableComponent;
  @ViewChild('benefitsTable', {static: false}) benefitsTable: GenericTableComponent;
  @ViewChild('projectsTable', {static: false}) projectsTable: GenericTableComponent;
  @ViewChild('regionsTable', {static: false}) regionsTable: GenericTableComponent;
  @ViewChild('publishersTable', {static: false}) publishersTable: GenericTableComponent;
  @ViewChild('subscribersTable', {static: false}) subscribersTable: GenericTableComponent;
  @ViewChild('documentationsTable', {static: false}) documentationsTable: GenericTableComponent;
  @ViewChild('cohortTable', {static: false}) cohortTable: GenericTableComponent;
  @ViewChild('dataSetTable', {static: false}) dataSetTable: GenericTableComponent;

  constructor(private log: LoggerService,
              private dsaService: DataSharingAgreementService,
              private documentationService: DocumentationService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerService,
              private datePipe: DatePipe,
              public dialog: MatDialog,
              private linkageService: ItemLinkageService) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
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
    this.dsa = {
      name : ''
    } as Dsa;
  }

  load(uuid: string) {
    this.dsaService.getDsa(uuid)
      .subscribe(result =>  {
          this.dsa = result;
          this.dsa.startDate = this.datePipe.transform(this.dsa.startDate,"yyyy-MM-dd");
          this.dsa.endDate = this.datePipe.transform(this.dsa.endDate,"yyyy-MM-dd");
          this.checkEndDate();
          this.getPurposes();
          this.getBenefits();
          this.getLinkedRegions();
          this.getProjects();
          this.getPublishers();
          this.getSubscribers();
          this.getPublisherMarkers();
          this.getSubscriberMarkers();
          this.getAssociatedDocumentation();
          this.getLinkedCohorts();
          this.getLinkedDataSets();
        },
        error => this.log.error('The data sharing agreement could not be loaded. Please try again.')
      );
  }

  checkEndDate() {
    if (this.dsa.endDate === null) {
      this.disableStatus = false;
      return;
    }

    let today = new Date();
    today.setHours(0,0,0,0);
    let endDate = new Date(this.dsa.endDate);

    if (endDate < today) {
      this.dsa.dsaStatusId = 1;
      this.disableStatus = true;
    } else {
      this.disableStatus = false;
    }
  }

  save(close: boolean) {
    this.dsaService.saveDsa(this.dsa)
      .subscribe(saved => {
          this.dsa.uuid = saved;
          this.log.success('Data sharing agreement saved');

          if (close) {this.close();}
        },
        error => this.log.error('The data sharing agreement could not be saved. Please try again.')
      );
  }

  close() {
    window.history.back();
  }

  purposeClicked(item: Purpose) {
    let index = this.purposes.indexOf(item);
    this.addPurpose(index);
  }

  benefitClicked(item: Purpose) {
    let index = this.benefits.indexOf(item);
    this.addBenefit(index);
  }

  regionClicked(item: Organisation) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  projectClicked(item: Project) {
    this.router.navigate(['/project', item.uuid, 'edit']);
  }

  publisherClicked(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  subscriberClicked(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  cohortClicked(item: Cohort) {
    this.router.navigate(['/cohort', item.uuid, 'edit']);
  }

  dataSetClicked(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
  }

  documentationClicked(item: Documentation) {
    const element = document.createElement('a');
    element.href = item.fileData;
    element.download = item.filename;
    document.body.appendChild(element);
    element.click();
  }

  private getLinkedRegions() {
    this.dsaService.getLinkedRegions(this.dsa.uuid, this.userId)
      .subscribe(
        result => {
          this.regions = result;
          this.regionsTable.updateRows();
        },
        error => this.log.error('The associated regions could not be loaded. Please try again.')
      );
  }

  private getPublishers() {
    this.dsaService.getPublishers(this.dsa.uuid)
      .subscribe(
        result => {
          this.publishers = result;
          this.publishersTable.updateRows();
        },
        error => this.log.error('The associated publishers could not be loaded. Please try again.')
      );
  }

  private getProjects() {
    this.dsaService.getProjects(this.dsa.uuid)
      .subscribe(
        result => {
          this.projects = result;
          this.projectsTable.updateRows();
        },
        error => this.log.error('The associated projects could not be loaded. Please try again.')
      );
  }

  private getSubscribers() {
    this.dsaService.getSubscribers(this.dsa.uuid)
      .subscribe(
        result => {
          this.subscribers = result;
          this.subscribersTable.updateRows();
        },
        error => this.log.error('The associated subscribers could not be loaded. Please try again.')
      );
  }

  private getPurposes() {
    this.dsaService.getPurposes(this.dsa.uuid)
      .subscribe(
        result => {
          this.purposes = result;
          this.purposesTable.updateRows();
        },
        error => this.log.error('The associated purposes could not be loaded. Please try again.')
      );
  }

  private getBenefits() {
    this.dsaService.getBenefits(this.dsa.uuid)
      .subscribe(
        result => {
          this.benefits = result;
          this.benefitsTable.updateRows();
        },
        error => this.log.error('The associated benefits could not be loaded. Please try again.')
      );
  }

  private getAssociatedDocumentation() {
    this.documentationService.getAllAssociatedDocuments(this.dsa.uuid, '3')
      .subscribe(
        result => {
          this.documentations = result;
          this.documentationsTable.updateRows();
        },
        error => this.log.error('The associated documents could not be loaded. Please try again.')
      );
  }

  private getSubscriberMarkers() {
    this.dsaService.getSubscriberMarkers(this.dsa.uuid)
      .subscribe(
        result => {
          this.subscriberMarkers = result;
        },
        error => this.log.error('The associated subscriber map data could not be loaded. Please try again.')
      )
  }

  private getPublisherMarkers() {
    this.dsaService.getPublisherMarkers(this.dsa.uuid)
      .subscribe(
        result => {
          this.mapMarkers = result;
          this.publisherMarkers = result;
        },
        error => this.log.error('The associated publisher map data could not be loaded. Please try again.')
      )
  }

  private getLinkedCohorts() {
    this.dsaService.getLinkedCohorts(this.dsa.uuid)
      .subscribe(
        result => {
          this.cohorts = result;
          this.cohortTable.updateRows();
        },
        error => this.log.error('The associated cohorts could not be loaded. Please try again.')
      );
  }

  private getLinkedDataSets() {
    this.dsaService.getLinkedDataSets(this.dsa.uuid)
      .subscribe(
        result => {
          this.dataSets = result;
          this.dataSetTable.updateRows();
        },
        error => this.log.error('The associated data sets could not be loaded. Please try again.')
      );
  }

  deletePurposes() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete purposes', 'Are you sure you want to delete purposes?',
      'Delete purposes', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.purposesTable.selection.selected.length; i++) {
              let purpose = this.purposesTable.selection.selected[i];
              this.purposes.forEach( (item, index) => {
                if(item === purpose) this.purposes.splice(index,1);
              });
            }
            this.clearMappings();
            this.dsa.purposes = [];
            this.dsa.purposes = this.purposes;
            this.updateMappings('Purposes');
          } else {
            this.log.success('Delete cancelled.')
          }
        },
      );

  }

  addPurpose(index: number) {
    const dialogRef = this.dialog.open(PurposeComponent, {
      width: '30vw',
      data: {resultData: this.purposes, type: 'Purpose', index: index},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.purposes = result;
        this.clearMappings();
        this.dsa.purposes = [];
        this.dsa.purposes = this.purposes;
        this.updateMappings('Purposes');
      }
    });
  }

  deleteBenefits() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete benefits', 'Are you sure you want to delete benefits?',
      'Delete benefits', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.benefitsTable.selection.selected.length; i++) {
              let purpose = this.benefitsTable.selection.selected[i];
              this.benefits.forEach( (item, index) => {
                if(item === purpose) this.benefits.splice(index,1);
              });
            }
            this.clearMappings();
            this.dsa.benefits = [];
            this.dsa.benefits = this.benefits;
            this.updateMappings('Benefits');
          } else {
            this.log.success('Delete cancelled.')
          }
        },
      );
  }

  addBenefit(index: number) {
    const dialogRef = this.dialog.open(PurposeComponent, {
      width: '550px',
      data: {resultData: this.benefits, type: 'Benefit', index: index},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.benefits = result;
        this.clearMappings();
        this.dsa.benefits = [];
        this.dsa.benefits = this.benefits;
        this.updateMappings('Benefits');
      }
    });
  }

  deleteRegions() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove regions', 'Are you sure you want to remove regions?',
      'Remove regions', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.regionsTable.selection.selected.length; i++) {
              let region = this.regionsTable.selection.selected[i];
              this.regions.forEach( (item, index) => {
                if(item === region) this.regions.splice(index,1);
              });
            }
            this.clearMappings();
            this.dsa.regions = {};
            for (const idx in this.regions) {
              const region: Region = this.regions[idx];
              this.dsa.regions[region.uuid] = region.name;
            }
            this.updateMappings('Regions');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addRegion() {
    const dialogRef = this.dialog.open(RegionPickerComponent, {
      minWidth: '50vw',
      data: { uuid: '', limit: 0, userId : this.activeProject.userId, existing: this.regions }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let region of result) {
        if (!this.regions.some(x => x.uuid === region.uuid)) {
          this.regions.push(region);
        }
      }
      this.clearMappings();
      this.dsa.regions = {};
      for (const idx in this.regions) {
        const region: Region = this.regions[idx];
        this.dsa.regions[region.uuid] = region.name;
      }
      this.updateMappings('Regions');
    });
  }

  deleteProjects() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove projects', 'Are you sure you want to remove projects?',
      'Remove projects', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.projectsTable.selection.selected.length; i++) {
              let project = this.projectsTable.selection.selected[i];
              this.projects.forEach( (item, index) => {
                if(item === project) this.projects.splice(index,1);
              });
            }
            this.clearMappings();
            this.dsa.projects = {};
            for (const idx in this.projects) {
              const project: Project = this.projects[idx];
              this.dsa.projects[project.uuid] = project.name;
            }
            this.updateMappings('Projects');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addProject() {
    const dialogRef = this.dialog.open(ProjectPickerComponent, {
      minWidth: '50vw',
      data: { uuid: '', limit: 0, userId : this.activeProject.userId, existing: this.projects }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let project of result) {
        if (!this.projects.some(x => x.uuid === project.uuid)) {
          this.projects.push(project);
        }
      }
      this.clearMappings();
      this.dsa.projects = {};
      for (const idx in this.projects) {
        const project: Project = this.projects[idx];
        this.dsa.projects[project.uuid] = project.name;
      }
      this.updateMappings('Projects');
    });
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
            this.dsa.publishers = {};
            for (const idx in this.publishers) {
              const publisher: Organisation = this.publishers[idx];
              this.dsa.publishers[publisher.uuid] = publisher.name;
            }
            this.updateMappings('Publishers');
          } else {
            this.log.success('Remove cancelled')
          }
        });
  }

  addPublisher() {
    if (!this.regions[0]) {
      this.log.error('The data sharing agreement must be associated with a region before editing publishers.');
    } else {
      const dialogRef = this.dialog.open(OrganisationPickerComponent, {
        minWidth: '50vw',
        data: { searchType: 'organisation', uuid: '', regionUUID: this.regions[0].uuid, dsaUUID: '', existingOrgs: this.publishers }
      });
      dialogRef.afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        for (let publisher of result) {
          if (!this.publishers.some(x => x.uuid === publisher.uuid)) {
            this.publishers.push(publisher);
          }
        }
        this.clearMappings();
        this.dsa.publishers = {};
        for (const idx in this.publishers) {
          const publisher: Organisation = this.publishers[idx];
          this.dsa.publishers[publisher.uuid] = publisher.name;
        }
        this.updateMappings('Publishers');
      });
    }
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
            this.dsa.subscribers = {};
            for (const idx in this.subscribers) {
              const subscriber: Organisation = this.subscribers[idx];
              this.dsa.subscribers[subscriber.uuid] = subscriber.name;
            }
            this.updateMappings('Subscribers');
          } else {
            this.log.success('Remove cancelled.')
          }
        });
  }

  addSubscriber() {
    if (!this.regions[0]) {
      this.log.error('The data sharing agreement must be associated with a region before editing subscribers.');
    } else {
      const dialogRef = this.dialog.open(OrganisationPickerComponent, {
        minWidth: '50vw',
        data: { searchType: 'organisation', uuid: '', regionUUID: this.regions[0].uuid, dsaUUID: '', existingOrgs: this.subscribers }
      });
      dialogRef.afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        for (let subscriber of result) {
          if (!this.subscribers.some(x => x.uuid === subscriber.uuid)) {
            this.subscribers.push(subscriber);
          }
        }
        this.clearMappings();
        this.dsa.subscribers = {};
        for (const idx in this.subscribers) {
          const subscriber: Organisation = this.subscribers[idx];
          this.dsa.subscribers[subscriber.uuid] = subscriber.name;
        }
        this.updateMappings('Subscribers');
      });
    }
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
            this.dsa.documentations = [];
            this.dsa.documentations = this.documentations;
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
        this.dsaService.addDocument(this.dsa.uuid, result)
          .subscribe(saved => {
              this.dsa.uuid = saved;
              this.log.success('Documents updated successfully.');
              this.getAssociatedDocumentation();
            },
            error => {
              this.log.error('The sharing agreement could not be saved. Please try again.');
              this.getAssociatedDocumentation();
            }
          );
      }
    });
  }

  addCohorts() {
    const dialogRef = this.dialog.open(CohortPickerComponent, {
      minWidth: '50vw',
      data: {userId: this.activeProject.userId, existing: this.cohorts}
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let coh of result) {
        if (!this.cohorts.some(x => x.uuid === coh.uuid)) {
          this.cohorts.push(coh);
        }
      }
      this.clearMappings();
      this.dsa.cohorts = {};

      for (const idx in this.cohorts) {
        const cohort: Cohort = this.cohorts[idx];
        this.dsa.cohorts[cohort.uuid] = cohort.name;
      }
      console.log(this.dsa);
      this.updateMappings('Cohorts');

    })
  }

  addDataSets() {
    const dialogRef = this.dialog.open(DataSetPickerComponent, {
      minWidth: '50vw',
      data: {userId: this.activeProject.userId, existing: this.dataSets},
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let ds of result) {
        if (!this.dataSets.some(x => x.uuid === ds.uuid)) {
          this.dataSets.push(ds);
        }
      }
      this.clearMappings();
      this.dsa.dataSets = {};
      for (const idx in this.dataSets) {
        const dataSet: DataSet = this.dataSets[idx];
        this.dsa.dataSets[dataSet.uuid] = dataSet.name;
      }
      this.updateMappings('DataSets');

    })
  }

  deleteDataSets() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove data sets', 'Are you sure you want to remove data sets?',
      'Remove data sets', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dataSetTable.selection.selected.length; i++) {
              let org = this.dataSetTable.selection.selected[i];
              this.dataSets.forEach( (item, index) => {
                if(item === org) this.dataSets.splice(index,1);
              });
            }
            this.clearMappings();
            this.dsa.dataSets = {};
            for (const idx in this.dataSets) {
              const dataSet: DataSet = this.dataSets[idx];
              this.dsa.dataSets[dataSet.uuid] = dataSet.name;
            }
            this.updateMappings('DataSets');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  deleteCohorts() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove cohorts', 'Are you sure you want to remove cohorts?',
      'Remove cohorts', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.cohortTable.selection.selected.length; i++) {
              let org = this.cohortTable.selection.selected[i];
              this.cohorts.forEach( (item, index) => {
                if(item === org) this.cohorts.splice(index,1);
              });
            }
            this.clearMappings();
            this.dsa.cohorts = {};
            for (const idx in this.cohorts) {
              const cohort: Cohort = this.cohorts[idx];
              this.dsa.cohorts[cohort.uuid] = cohort.name;
            }
            this.updateMappings('Cohorts');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  swapMarkers() {
    if (this.showPub) {
      this.mapMarkers = this.publisherMarkers;
    } else {
      this.mapMarkers = this.subscriberMarkers;
    }
  }

  editDSA() {
    const dialogRef = this.dialog.open(DataSharingAgreementDialogComponent, {
      data: {mode: 'edit', uuid: this.dsa.uuid },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.dsa = result;
        this.log.success('Data sharing agreement saved.');
      }
    });
  }

  updateMappings(type: string) {
    this.dsaService.updateMappings(this.dsa)
      .subscribe(saved => {
          this.dsa.uuid = saved;
          this.log.success(type + ' updated successfully.');
          this.refreshMappings(type);
        },
        error => {
          this.log.error('The sharing agreement could not be saved. Please try again.');
          this.refreshMappings(type);
        }
      );
  }

  refreshMappings(type: string) {
    if (type == 'Purposes') {
      this.getPurposes();
    } else if (type == 'Benefits') {
      this.getBenefits();
    } else if (type == 'Regions') {
      this.getLinkedRegions()
    } else if (type == 'Projects') {
      this.getProjects()
    } else if (type == 'Publishers') {
      this.getPublishers();
      this.getPublisherMarkers();
    } else if (type == 'Subscribers') {
      this.getSubscribers();
      this.getSubscriberMarkers();
    } else if (type == 'Documents') {
      this.getAssociatedDocumentation();
    } else if (type == 'Cohorts') {
      this.getLinkedCohorts();
    } else if (type == 'DataSets') {
      this.getLinkedDataSets();
    }
  }

  showPublishers() {
    this.showMap(this.publisherMarkers, 'Location of publishers');
  }

  showSubscribers() {
    this.showMap(this.subscriberMarkers, 'Location of subscribers');
  }

  showMap(selectedMarkers: Marker[], title) {
    const dialogRef = this.dialog.open(GoogleMapsDialogComponent, {
      minWidth: '60vw',
      data: {markers: selectedMarkers, title: title}
    })
    dialogRef.afterClosed().subscribe(result => {
      return;
    })
  }

  clearMappings() {
    this.dsa.purposes = null;
    this.dsa.benefits = null;
    this.dsa.regions = null;
    this.dsa.projects = null;
    this.dsa.publishers = null;
    this.dsa.subscribers = null;
    this.dsa.documentations = null;
    this.dsa.cohorts = null;
    this.dsa.dataSets = null;
  }
}
