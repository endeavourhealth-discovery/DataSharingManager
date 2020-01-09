import {Component, OnInit, ViewChild} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute, Router} from '@angular/router';
import {LoggerService, UserManagerService} from "dds-angular8";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Dsa} from '../models/Dsa';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
//import {DataFlow} from '../../data-flow/models/DataFlow';
//import {DataflowPickerComponent} from '../../data-flow/dataflow-picker/dataflow-picker.component';
import {Purpose} from '../../models/Purpose';
import {PurposeComponent} from "../../purpose/purpose/purpose.component";
import {Region} from '../../region/models/Region';
import {Marker} from '../../region/models/Marker';
import {RegionPickerComponent} from '../../region/region-picker/region-picker.component';
import {Project} from "../../project/models/Project";
//import {ProjectPickerComponent} from "../../project/project-picker/project-picker.component";
import {Organisation} from '../../organisation/models/Organisation';
import {OrganisationPickerComponent} from '../../organisation/organisation-picker/organisation-picker.component';
import {Documentation} from "../../documentation/models/Documentation";
import {DocumentationService} from "../../documentation/documentation.service";
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";


@Component({
  selector: 'app-data-sharing-agreement-editor',
  templateUrl: './data-sharing-agreement-editor.component.html',
  styleUrls: ['./data-sharing-agreement-editor.component.css']
})

export class DataSharingAgreementEditorComponent implements OnInit {
  private paramSubscriber: any;

  dsa: Dsa = <Dsa>{};
  //dataFlows: DataFlow[] = [];
  purposes: Purpose[] = [];
  benefits: Purpose[] = [];
  regions: Region[] = [];
  projects: Project[] = [];
  publishers: Organisation[] = [];
  subscribers: Organisation[] = [];
  documentations: Documentation[] = [];

  publisherMarkers: Marker[];
  subscriberMarkers: Marker[];
  mapMarkers: Marker[] = [];
  showPub = true;
  allowEdit = false;
  file: File;
  pdfSrc: any;
  disableStatus = false;
  superUser = false;
  userId: string;

  model = 1;

  public activeProject: UserProject;

  status = [
    {num: 0, name : 'Active'},
    {num: 1, name : 'Inactive'}
  ];

  consents = [
    {num: 0, name : 'Explicit Consent'},
    {num: 1, name : 'Implied Consent'}
  ];

  //dataflowDetailsToShow = new DataFlow().getDisplayItems();
  purposeDetailsToShow = new Purpose().getDisplayItems();
  benefitDetailsToShow = new Purpose().getDisplayItems();
  regionDetailsToShow = new Region().getDisplayItems();
  projectDetailsToShow = new Project().getDisplayItems();
  publisherDetailsToShow = new Organisation().getDisplayItems();
  subscriberDetailsToShow = new Organisation().getDisplayItems();
  documentDetailsToShow = new Documentation().getDisplayItems();

  @ViewChild('purposesTable', {static: false}) purposesTable: GenericTableComponent;
  @ViewChild('benefitsTable', {static: false}) benefitsTable: GenericTableComponent;
  @ViewChild('projectsTable', {static: false}) projectsTable: GenericTableComponent;
  @ViewChild('regionsTable', {static: false}) regionsTable: GenericTableComponent;
  @ViewChild('publishersTable', {static: false}) publishersTable: GenericTableComponent;
  @ViewChild('subscribersTable', {static: false}) subscribersTable: GenericTableComponent;
  @ViewChild('documentationsTable', {static: false}) documentationsTable: GenericTableComponent;

  constructor(private log: LoggerService,
              private dsaService: DataSharingAgreementService,
              private documentationService: DocumentationService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerService,
              private datePipe: DatePipe,
              public dialog: MatDialog) {
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
          //this.getLinkedDataFlows();
          this.getPurposes();
          this.getBenefits();
          this.getLinkedRegions();
          this.getProjects();
          this.getPublishers();
          this.getSubscribers();
          this.getPublisherMarkers();
          this.getSubscriberMarkers();
          this.getAssociatedDocumentation();
        },
        error => this.log.error('The data sharing agreement could not be loaded. Please try again.'/*, error, 'Load data sharing agreement'*/)
      );
  }

  save(close: boolean) {

    /*// Populate data flows before save
    vm.dsa.dataFlows = {};
    for (const idx in this.dataFlows) {
      const dataflow: DataFlow = this.dataFlows[idx];
      this.dsa.dataFlows[dataflow.uuid] = dataflow.name;
    }*/

    // Populate purposes before save
    this.dsa.purposes = [];
    this.dsa.purposes = this.purposes;

    // Populate benefits before save
    this.dsa.benefits = [];
    this.dsa.benefits = this.benefits;

    // Populate regions before save
    this.dsa.regions = {};
    for (const idx in this.regions) {
      const region: Region = this.regions[idx];
      this.dsa.regions[region.uuid] = region.name;
    }

    // Populate projects before save
    this.dsa.projects = {};
    for (const idx in this.projects) {
      const proj: Project = this.projects[idx];
      this.dsa.projects[proj.uuid] = proj.name;
    }

    // Populate publishers before save
    this.dsa.publishers = {};
    for (const idx in this.publishers) {
      const pub: Organisation = this.publishers[idx];
      this.dsa.publishers[pub.uuid] = pub.name;
    }

    // Populate subscribers before save
    this.dsa.subscribers = {};
    for (const idx in this.subscribers) {
      const sub: Organisation = this.subscribers[idx];
      this.dsa.subscribers[sub.uuid] = sub.name;
    }

    // Populate documents before save
    this.dsa.documentations = [];
    this.dsa.documentations = this.documentations;

    this.dsaService.saveDsa(this.dsa)
      .subscribe(saved => {
          this.dsa.uuid = saved;
          this.log.success('Data sharing agreement saved'/*, vm.dsa, 'Save data sharing agreement'*/);

          if (close) {this.close();}
        },
        error => this.log.error('The data sharing agreement could not be saved. Please try again.'/*, error, 'Save data sharing agreement'*/)
      );
  }

  close() {
    window.history.back();
  }

  /*private editDataFlow(item: DataFlow) {
  this.router.navigate(['/dataFlow', item.uuid, 'edit']);
  }

  private editDataFlows() {
    const vm = this;
    DataflowPickerComponent.open(vm.$modal, vm.dataFlows)
      .result.then(function
      (result: DataFlow[]) { vm.dataFlows = result; },
      () => vm.log.info('Edit data flows cancelled')
    );
  }*/

  private editRegions() {
    /*RegionPickerComponent.open(this.$modal, this.regions, '', 1)
      .result.then(function
      (result: Region[]) {this.regions = result;},
      () => this.log.info('Edit regions cancelled')
    );*/
  }

  private editProjects() {
    /*ProjectPickerComponent.open(this.$modal, this.projects)
      .result.then(function
      (result: Project[]) {this.projects = result;},
      () => this.log.info('Edit projects cancelled')
    );*/
  }


  private editPublishers() {
    /*if (!this.regions[0]) {
      MessageBoxDialog.open(this.$modal, 'Edit publishers', 'The data sharing agreement must be associated with a region before editing publishers', 'Ok', '')
        .result.then();
    } else {
      OrganisationPickerComponent.open(this.$modal, this.publishers, 'publisher', '', this.regions[0].uuid, '')
        .result.then(function
        (result: Organisation[]) {
          this.publishers = result;
        },
        () => this.log.info('Edit publishers cancelled')
      );
    }*/
  }

  private editSubscribers() {
    /*if (!this.regions[0]) {
      MessageBoxDialog.open(this.$modal, 'Edit subscribers', 'The data sharing agreement must be associated with a region before editing subscribers', 'Ok', '')
        .result.then();
    } else {
      OrganisationPickerComponent.open(this.$modal, this.subscribers, 'subscriber', '', this.regions[0].uuid, '')
        .result.then(function
        (result: Organisation[]) {
          this.subscribers = result;
        },
        () => this.log.info('Edit subscribers cancelled')
      );
    }*/
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
    this.router.navigate(['/project]', item.uuid, 'edit']);
  }

  publisherClicked(item: Organisation) {
    this.router.navigate(['/organisation]', item.uuid, 'edit']);
  }

  subscriberClicked(item: Organisation) {
    this.router.navigate(['/organisation]', item.uuid, 'edit']);
  }

  documentationClicked(item: Documentation) {
    this.router.navigate(['/documentation]', item.uuid, 'edit']);
  }

  /*private getLinkedDataFlows() {
    const vm = this;
    vm.dsaService.getLinkedDataFlows(vm.dsa.uuid)
      .subscribe(
        result => vm.dataFlows = result,
        error => vm.log.error('The associated data flows could not be loaded. Please try again.', error, 'Load associated data flows')
      );
  }*/

  private getLinkedRegions() {
    this.dsaService.getLinkedRegions(this.dsa.uuid, this.userId)
      .subscribe(
        result => this.regions = result,
        error => this.log.error('The associated regions could not be loaded. Please try again.'/*, error, 'Load associated regions'*/)
      );
  }

  private getPublishers() {
    this.dsaService.getPublishers(this.dsa.uuid)
      .subscribe(
        result => this.publishers = result,
        error => this.log.error('The associated publishers could not be loaded. Please try again.'/*, error, 'Load associated publishers'*/)
      );
  }

  private getProjects() {
    this.dsaService.getProjects(this.dsa.uuid)
      .subscribe(
        result => this.projects = result,
        error => this.log.error('The associated projects could not be loaded. Please try again.'/*, error, 'Load associated projects'*/)
      );
  }

  private getSubscribers() {
    this.dsaService.getSubscribers(this.dsa.uuid)
      .subscribe(
        result => this.subscribers = result,
        error => this.log.error('The associated subscribers could not be loaded. Please try again.'/*, error, 'Load associated subscribers'*/)
      );
  }

  private getPurposes() {
    this.dsaService.getPurposes(this.dsa.uuid)
      .subscribe(
        result => this.purposes = result,
        error => this.log.error('The associated purposes could not be loaded. Please try again.'/*, error, 'Load associated purposes'*/)
      );
  }

  private getBenefits() {
    this.dsaService.getBenefits(this.dsa.uuid)
      .subscribe(
        result => this.benefits = result,
        error => this.log.error('The associated benefits could not be loaded. Please try again.'/*, error, 'Load associated benefits'*/)
      );
  }

  private getAssociatedDocumentation() {
    this.documentationService.getAllAssociatedDocuments(this.dsa.uuid, '3')
      .subscribe(
        result => this.documentations = result,
        error => this.log.error('The associated documentation could not be loaded. Please try again.'/*, error, 'Load associated documentation'*/)
      );
  }

  private getSubscriberMarkers() {
    this.dsaService.getSubscriberMarkers(this.dsa.uuid)
      .subscribe(
        result => {
          this.subscriberMarkers = result;
        },
        error => this.log.error('The associated subscriber map data could not be loaded. Please try again.'/*, error, 'Load subscriber map data'*/)
      )
  }

  private getPublisherMarkers() {
    this.dsaService.getPublisherMarkers(this.dsa.uuid)
      .subscribe(
        result => {
          this.mapMarkers = result;
          this.publisherMarkers = result;
        },
        error => this.log.error('The associated publisher map data could not be loaded. Please try again.'/*, error, 'Load publisher map data'*/)
      )
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

  swapMarkers() {
    if (this.showPub) {
      this.mapMarkers = this.publisherMarkers;
    } else {
      this.mapMarkers = this.subscriberMarkers;
    }
  }

  private uploadFile() {
    const vm = this;
    const myReader: FileReader = new FileReader();

    myReader.onloadend = function(e){
      // you can perform an action with readed data here
      vm.log.success('Uploading document'/*, null, 'Upload document'*/);
      vm.pdfSrc = myReader.result;
      const newDoc: Documentation = new Documentation();
      // Compile error for line below: Type 'string | ArrayBuffer' is not assignable to type 'string'.
      // newDoc.fileData = myReader.result;
      newDoc.title = vm.file.name;
      newDoc.filename = vm.file.name;
      vm.documentations.push(newDoc);
    };

    myReader.readAsDataURL(vm.file);
  }

  fileChange(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file = fileList[0];
    } else {
      this.file = null;
    }
  }

  ok() {
    this.uploadFile();
  }

  cancel() {
    this.file = null;
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

}
