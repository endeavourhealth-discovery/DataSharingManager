import {Component, OnInit, ViewChild} from '@angular/core';
import {Dpa} from "../models/Dpa";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Organisation} from "../../organisation/models/Organisation";
import {Documentation} from "../../documentation/models/Documentation";
import {LoggerService, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from "@angular/router";
import {DataProcessingAgreementService} from "../data-processing-agreement.service";
import {DatePipe} from "@angular/common";
import {Region} from "../../region/models/Region";
import {DocumentationService} from "../../documentation/documentation.service";
import {Purpose} from "src/app/models/Purpose";
import {MatDialog} from "@angular/material/dialog";
import {PurposeComponent} from "../../purpose/purpose/purpose.component";
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";
import {DocumentationComponent} from "../../documentation/documentation/documentation.component";

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


  dpa: Dpa;
  public activeProject: UserProject;
  private paramSubscriber: any;
  allowEdit = false;
  superUser = false;
  userId: string;
  disableStatus = false;

  purposes: Purpose[] = [];
  benefits: Purpose[] = [];
  purposesDetailsToShow = new Purpose().getDisplayItems();

  regions: Region[] = [];
  regionsDetailsToShow = new Region().getDisplayItems();
  publishers: Organisation[] = [];
  publishersDetailsToShow = new Organisation().getDisplayItems();
  documentations: Documentation[] = [];
  documentationsDetailsToShow = new Documentation().getDisplayItems();

  status = [
    {num: 0, name: 'Active'},
    {num: 1, name: 'Inactive'}
  ];
  processor = 'Discovery';

  constructor(private log: LoggerService,
              private dpaService: DataProcessingAgreementService,
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
    const vm = this;
    vm.dpaService.getPurposes(vm.dpa.uuid)
      .subscribe(
        result => vm.purposes = result,
        error => vm.log.error('The associated purposes could not be loaded. Please try again.')
      );
  }

  purposeClicked(item: Purpose) {
    let index = this.purposes.indexOf(item);
    this.addPurpose(index);
  }

  deletePurposes() {
    for (var i = 0; i < this.purposesTable.selection.selected.length; i++) {
      let purpose = this.purposesTable.selection.selected[i];
      this.purposes.forEach( (item, index) => {
        if(item === purpose) this.purposes.splice(index,1);
      });
    }
    this.purposesTable.updateRows();
  }

  addPurpose(index: number) {
    const dialogRef = this.dialog.open(PurposeComponent, {
      height: '580px',
      width: '550px',
      data: {resultData: this.purposes, type: 'Purpose', index: index},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.purposes = result;
        this.purposesTable.updateRows();
      }
    });
  }

  getBenefits() {
    this.dpaService.getBenefits(this.dpa.uuid)
      .subscribe(
        result => this.benefits = result,
        error => this.log.error('The associated benefits could not be loaded. Please try again.')
      );
  }

  benefitClicked(item: Purpose) {
    let index = this.benefits.indexOf(item);
    this.addBenefit(index);
  }

  deleteBenefits() {
    for (var i = 0; i < this.benefitsTable.selection.selected.length; i++) {
      let purpose = this.benefitsTable.selection.selected[i];
      this.benefits.forEach( (item, index) => {
        if(item === purpose) this.benefits.splice(index,1);
      });
    }
    this.benefitsTable.updateRows();
  }

  addBenefit(index: number) {
    const dialogRef = this.dialog.open(PurposeComponent, {
      height: '580px',
      width: '550px',
      data: {resultData: this.benefits, type: 'Benefit', index: index},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.benefits = result;
        this.benefitsTable.updateRows();
      }
    });
  }

  getRegions() {
    this.dpaService.getLinkedRegions(this.dpa.uuid, this.userId)
      .subscribe(
        result => this.regions = result,
        error => this.log.error('The associated regions could not be loaded. Please try again.')
      );
  }

  regionClicked(item: Region) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  deleteRegions() {
    for (var i = 0; i < this.regionsTable.selection.selected.length; i++) {
      let purpose = this.regionsTable.selection.selected[i];
      this.regions.forEach( (item, index) => {
        if(item === purpose) this.regions.splice(index,1);
      });
    }
    this.regionsTable.updateRows();
  }

  addRegion() {
    this.router.navigate(['/region', 1, 'add']);
  }

  getPublishers() {
    this.dpaService.getPublishers(this.dpa.uuid)
      .subscribe(
        result => this.publishers = result,
        error => this.log.error('The associated publishers could not be loaded. Please try again.')
      );
  }

  publisherClicked(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  deletePublishers() {
    for (var i = 0; i < this.publishersTable.selection.selected.length; i++) {
      let purpose = this.publishersTable.selection.selected[i];
      this.publishers.forEach( (item, index) => {
        if(item === purpose) this.publishers.splice(index,1);
      });
    }
    this.publishersTable.updateRows();
  }

  addPublisher() {
    this.router.navigate(['/organisation', 1, 'add']);
  }

  getDocumentations() {
    this.documentationService.getAllAssociatedDocuments(this.dpa.uuid, '5')
      .subscribe(
        result => this.documentations = result,
        error => this.log.error('The associated documentation could not be loaded. Please try again.')
      );
  }

  deleteDocumentations() {
    for (var i = 0; i < this.documentationsTable.selection.selected.length; i++) {
      let purpose = this.documentationsTable.selection.selected[i];
      this.documentations.forEach( (item, index) => {
        if(item === purpose) this.documentations.splice(index,1);
      });
    }
    this.documentationsTable.updateRows();
  }

  addDocumentation() {
    const dialogRef = this.dialog.open(DocumentationComponent, {
      height: '350px',
      width: '550px',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.documentations.push(result);
        this.documentationsTable.updateRows();
      }
    });
  }


  save(close: boolean) {

    // Populate purposes before save
    this.dpa.purposes = [];
    this.dpa.purposes = this.purposes;

    // Populate benefits before save
    this.dpa.benefits = [];
    this.dpa.benefits = this.benefits;

    // Populate regions before save
    this.dpa.regions = {};
    for (const idx in this.regions) {
      const region: Region = this.regions[idx];
      this.dpa.regions[region.uuid] = region.name;
    }

    // Populate publishers before save
    this.dpa.publishers = {};
    for (const idx in this.publishers) {
      const pub: Organisation = this.publishers[idx];
      this.dpa.publishers[pub.uuid] = pub.name;
    }

    // Populate documents before save
    this.dpa.documentations = [];
    this.dpa.documentations = this.documentations;

    this.dpaService.saveDpa(this.dpa)
      .subscribe(saved => {
          this.dpa.uuid = saved;
          this.log.success('Data processing agreement saved');
          if (close) {
            window.history.back();
          }
        },
        error => this.log.error('The data processing agreement could not be saved. Please try again.')
      );
  }
}
