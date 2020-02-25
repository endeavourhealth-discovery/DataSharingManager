import {Component, OnInit, ViewChild} from '@angular/core';
import {Dpa} from "../models/Dpa";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Organisation} from "../../organisation/models/Organisation";
import {Documentation} from "../../documentation/models/Documentation";
import {
  GenericTableComponent,
  ItemLinkageService,
  LoggerService,
  MessageBoxDialogComponent,
  UserManagerService
} from "dds-angular8";
import {ActivatedRoute, Router} from "@angular/router";
import {DataProcessingAgreementService} from "../data-processing-agreement.service";
import {DatePipe} from "@angular/common";
import {Region} from "../../region/models/Region";
import {DocumentationService} from "../../documentation/documentation.service";
import {Purpose} from "src/app/models/Purpose";
import {MatDialog} from "@angular/material/dialog";
import {PurposeComponent} from "../../purpose/purpose/purpose.component";
import {DocumentationComponent} from "../../documentation/documentation/documentation.component";
import {RegionPickerComponent} from "../../region/region-picker/region-picker.component";
import {OrganisationPickerComponent} from "../../organisation/organisation-picker/organisation-picker.component";
import {DataProcessingAgreementDialogComponent} from "../data-processing-agreement-dialog/data-processing-agreement-dialog.component";
import {Marker} from "../../region/models/Marker";
import {GoogleMapsDialogComponent} from "../../google-maps-viewer/google-maps-dialog/google-maps-dialog.component";
import {Cohort} from "../../cohort/models/Cohort";
import {DataSet} from "../../data-set/models/Dataset";
import {DataSetPickerComponent} from "../../data-set/data-set-picker/data-set-picker.component";
import {CohortPickerComponent} from "../../cohort/cohort-picker/cohort-picker.component";

@Component({
  selector: 'app-data-processing-agreement-editor',
  templateUrl: './data-processing-agreement-editor.component.html',
  styleUrls: ['./data-processing-agreement-editor.component.scss']
})
export class DataProcessingAgreementEditorComponent implements OnInit {

  @ViewChild('purposesTable', { static: false }) purposesTable: GenericTableComponent;
  @ViewChild('benefitsTable', { static: false }) benefitsTable: GenericTableComponent;
  @ViewChild('regionsTable', { static: false }) regionsTable: GenericTableComponent;
  @ViewChild('publishersTable', { static: false }) publishersTable: GenericTableComponent;
  @ViewChild('documentationsTable', { static: false }) documentationsTable: GenericTableComponent;
  @ViewChild('cohortTable', {static: false}) cohortTable: GenericTableComponent;
  @ViewChild('dataSetTable', {static: false}) dataSetTable: GenericTableComponent;


  dpa: Dpa;
  public activeProject: UserProject;
  private paramSubscriber: any;
  allowEdit = false;
  superUser = false;
  userId: string;
  disableStatus = false;
  publisherMarkers: Marker[] = [];

  purposes: Purpose[] = [];
  benefits: Purpose[] = [];
  purposesDetailsToShow = new Purpose().getDisplayItems();

  regions: Region[] = [];
  regionsDetailsToShow = new Region().getDisplayItems();
  publishers: Organisation[] = [];
  publishersDetailsToShow = new Organisation().getDisplayItems();
  documentations: Documentation[] = [];
  documentationsDetailsToShow = new Documentation().getDisplayItems();
  cohorts: Cohort[] = [];
  dataSets: DataSet[] = [];
  cohortDetailsToShow = new Cohort().getDisplayItems();
  dataSetDetailsToShow = new DataSet().getDisplayItems();

  status = this.linkageService.status;

  processor = 'Discovery';

  constructor(private log: LoggerService,
              private dpaService: DataProcessingAgreementService,
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
    this.dpa = {
      name : ''
    } as Dpa;
  }

  load(uuid: string) {
    this.dpaService.getDpa(uuid)
      .subscribe(result =>  {
          this.dpa = result;
          this.dpa.startDate = this.datePipe.transform(this.dpa.startDate,"yyyy-MM-dd");
          this.dpa.endDate = this.datePipe.transform(this.dpa.endDate,"yyyy-MM-dd");
          this.checkEndDate();
          this.getPurposes();
          this.getBenefits();
          this.getRegions();
          this.getPublishers();
          this.getDocumentations();
          this.getPublisherMarkers();
          this.getLinkedCohorts();
          this.getLinkedDataSets();
        },
        error => this.log.error('The data processing agreement could not be loaded. Please try again.')
      );
  }

  checkEndDate() {
    if (this.dpa.endDate === null) {
      this.disableStatus = false;
      return;
    }

    let today = new Date();
    today.setHours(0,0,0,0);
    let endDate = new Date(this.dpa.endDate);

    if (endDate < today) {
      this.dpa.dsaStatusId = 1;
      this.disableStatus = true;
    } else {
      this.disableStatus = false;
    }
  }

  private getPurposes() {
    this.dpaService.getPurposes(this.dpa.uuid)
      .subscribe(
        result => {
          this.purposes = result,
          this.purposesTable.updateRows();
        },
        error => this.log.error('The associated purposes could not be loaded. Please try again.')
      );
  }

  private getPublisherMarkers() {
    const vm = this;
    vm.dpaService.getPublisherMarkers(vm.dpa.uuid)
      .subscribe(
        result => {
          vm.publisherMarkers = result;
        },
        error => vm.log.error('The publisher map date could not be loaded. Please try again.')
      )
  }

  purposeClicked(item: Purpose) {
    let index = this.purposes.indexOf(item);
    this.addPurpose(index);
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
            this.dpa.purposes = [];
            this.dpa.purposes = this.purposes;
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
        this.dpa.purposes = [];
        this.dpa.purposes = this.purposes;
        this.updateMappings('Purposes');
      }
    });
  }

  getBenefits() {
    this.dpaService.getBenefits(this.dpa.uuid)
      .subscribe(
        result => {
          this.benefits = result;
          this.benefitsTable.updateRows();
        },
        error => this.log.error('The associated benefits could not be loaded. Please try again.')
      );
  }

  benefitClicked(item: Purpose) {
    let index = this.benefits.indexOf(item);
    this.addBenefit(index);
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
            this.dpa.benefits = [];
            this.dpa.benefits = this.benefits;
            this.updateMappings('Benefits');
          } else {
            this.log.success('Delete cancelled.')
          }
        },
      );
  }

  addBenefit(index: number) {
    const dialogRef = this.dialog.open(PurposeComponent, {
      width: '30vw',
      data: {resultData: this.benefits, type: 'Benefit', index: index},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.benefits = result;
        this.clearMappings();
        this.dpa.benefits = [];
        this.dpa.benefits = this.benefits;
        this.updateMappings('Benefits');
      }
    });
  }

  getRegions() {
    this.dpaService.getLinkedRegions(this.dpa.uuid, this.userId)
      .subscribe(
        result => {
          this.regions = result;
          this.regionsTable.updateRows();
        },
        error => this.log.error('The associated regions could not be loaded. Please try again.')
      );
  }

  private getLinkedCohorts() {
    this.dpaService.getLinkedCohorts(this.dpa.uuid)
      .subscribe(
        result => {
          this.cohorts = result;
          this.cohortTable.updateRows();
        },
        error => this.log.error('The associated cohorts could not be loaded. Please try again.')
      );
  }

  private getLinkedDataSets() {
    this.dpaService.getLinkedDataSets(this.dpa.uuid)
      .subscribe(
        result => {
          this.dataSets = result;
          this.dataSetTable.updateRows();
        },
        error => this.log.error('The associated data sets could not be loaded. Please try again.')
      );
  }

  regionClicked(item: Region) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  cohortClicked(item: Cohort) {
    this.router.navigate(['/cohort', item.uuid, 'edit']);
  }

  dataSetClicked(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
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
            this.dpa.regions = {};
            for (const idx in this.regions) {
              const region: Region = this.regions[idx];
              this.dpa.regions[region.uuid] = region.name;
            }
            this.updateMappings('Regions');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addRegion() {
    const dialogRef = this.dialog.open(RegionPickerComponent, {
      minWidth: '50vw',
      data: { uuid: '', limit: 0, userId : this.activeProject.userId, existing: this.regions }
    })
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
      this.dpa.regions = {};
      for (const idx in this.regions) {
        const region: Region = this.regions[idx];
        this.dpa.regions[region.uuid] = region.name;
      }
      this.updateMappings('Regions');
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
            this.dpa.dataSets = {};
            for (const idx in this.dataSets) {
              const ds: DataSet = this.dataSets[idx];
              this.dpa.dataSets[ds.uuid] = ds.name;
            }
            this.updateMappings('Data sets');
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
            this.dpa.cohorts = {};
            for (const idx in this.cohorts) {
              const coh: Cohort = this.cohorts[idx];
              this.dpa.cohorts[coh.uuid] = coh.name;
            }
            this.updateMappings('Cohorts');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addCohorts() {
    const dialogRef = this.dialog.open(CohortPickerComponent, {
      minWidth: '50vw',
      data: {existing: this.cohorts}
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let coh of result) {
        if (!this.cohorts.some(x => x.uuid === coh.uuid)) {
          this.cohorts.push(coh);
        }
        this.clearMappings();
        this.dpa.cohorts = {};
        for (const idx in this.cohorts) {
          const cohort: Cohort = this.cohorts[idx];
          this.dpa.cohorts[cohort.uuid] = cohort.name;
        }
        this.updateMappings('Cohorts');
      }
    })
  }

  addDataSets() {
    const dialogRef = this.dialog.open(DataSetPickerComponent, {
      minWidth: '50vw',
      data: {existing: this.dataSets},
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let ds of result) {
        if (!this.dataSets.some(x => x.uuid === ds.uuid)) {
          this.dataSets.push(ds);
        }
        this.clearMappings();
        this.dpa.dataSets = {};
        for (const idx in this.dataSets) {
          const dataSet: DataSet = this.dataSets[idx];
          this.dpa.dataSets[dataSet.uuid] = dataSet.name;
        }
        this.updateMappings('Data sets');
      }
    })
  }

  getPublishers() {
    this.dpaService.getPublishers(this.dpa.uuid)
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
              let purpose = this.publishersTable.selection.selected[i];
              this.publishers.forEach( (item, index) => {
                if(item === purpose) this.publishers.splice(index,1);
              });
            }
            this.clearMappings();
            this.dpa.publishers = {};
            for (const idx in this.publishers) {
              const pub: Organisation = this.publishers[idx];
              this.dpa.publishers[pub.uuid] = pub.name;
            }
            this.updateMappings('Publishers');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addPublisher() {
    if (!this.regions[0]) {
      this.log.error('The data processing agreement must be associated with a region before editing publishers.');
    } else {
      const dialogRef = this.dialog.open(OrganisationPickerComponent, {
        minWidth: '50vw',
        data: { searchType: 'organisation', uuid: '', regionUUID: this.regions[0].uuid, dsaUUID: '', existingOrgs: this.publishers }
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
        this.dpa.publishers = {};
        for (const idx in this.publishers) {
          const pub: Organisation = this.publishers[idx];
          this.dpa.publishers[pub.uuid] = pub.name;
        }
        this.updateMappings('Publishers');
      })
    }  }

  getDocumentations() {
    this.documentationService.getAllAssociatedDocuments(this.dpa.uuid, '5')
      .subscribe(
        result => {
          this.documentations = result;
          this.documentationsTable.updateRows();
        },
        error => this.log.error('The associated documentation could not be loaded. Please try again.')
      );
  }

  deleteDocumentations() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete documents', 'Are you sure you want to delete documents?',
      'Delete documents', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.documentationsTable.selection.selected.length; i++) {
              let purpose = this.documentationsTable.selection.selected[i];
              this.documentations.forEach( (item, index) => {
                if(item === purpose) this.documentations.splice(index,1);
              });
            }
            this.clearMappings();
            this.dpa.documentations = [];
            this.dpa.documentations = this.documentations;
            this.updateMappings('Documentations');
          } else {
            this.log.success('Delete cancelled.')
          }
        },
      );
  }

  documentationClicked(item: Documentation) {
    const element = document.createElement('a');
    element.href = item.fileData;
    element.download = item.filename;
    document.body.appendChild(element);
    element.click();
  }

  addDocumentation() {
    const dialogRef = this.dialog.open(DocumentationComponent, {
      width: '30vw',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.dpaService.addDocument(this.dpa.uuid, result)
          .subscribe(saved => {
              this.dpa.uuid = saved;
              this.log.success('Documents updated successfully.');
              this.getDocumentations();
            },
            error => {
              this.log.error('The processing agreement could not be saved. Please try again.');
              this.getDocumentations();
            }
          );
      }
    });
  }

  editDPA() {
    const dialogRef = this.dialog.open(DataProcessingAgreementDialogComponent, {
      data: {mode: 'edit', uuid: this.dpa.uuid },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.dpa = result;
        this.log.success('Data processing agreement saved.');
      }
    });
  }

  updateMappings(type: string) {
    this.dpaService.updateMappings(this.dpa)
      .subscribe(saved => {
          this.dpa.uuid = saved;
          this.log.success(type + ' updated successfully.');
          this.refreshMappings(type);
        },
        error => {
          this.log.error('The data processing agreement could not be saved. Please try again.')
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
      this.getRegions()
    } else if (type == 'Publishers') {
      this.getPublishers();
      this.getPublisherMarkers();
    } else if (type == 'Documentations') {
      this.getDocumentations()
    } else if (type == 'Cohorts') {
      this.getLinkedCohorts()
    } else if (type == 'Data sets') {
      this.getLinkedDataSets()
    }
  }

  showMap() {
    const dialogRef = this.dialog.open(GoogleMapsDialogComponent, {
      minWidth: '60vw',
      data: {markers: this.publisherMarkers, title: 'Location of publishers'}
    })
    dialogRef.afterClosed().subscribe(result => {
      return;
    })
  }

  clearMappings() {
    this.dpa.purposes = null;
    this.dpa.benefits = null;
    this.dpa.regions = null;
    this.dpa.publishers = null;
    this.dpa.documentations = null;
    this.dpa.cohorts = null;
    this.dpa.dataSets = null;
  }

  close() {
    window.history.back();
  }
}
