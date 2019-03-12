import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal, NgbRadioGroup} from '@ng-bootstrap/ng-bootstrap';
import {Purpose} from '../models/Purpose';
import {Organisation} from '../../organisation/models/Organisation';
import {Region} from '../../region/models/Region';
import {DataFlow} from '../../data-flow/models/DataFlow';
import {Dsa} from '../models/Dsa';
import {DataflowPickerComponent} from '../../data-flow/dataflow-picker/dataflow-picker.component';
import {RegionPickerComponent} from '../../region/region-picker/region-picker.component';
import {OrganisationPickerComponent} from '../../organisation/organisation-picker/organisation-picker.component';
import {PurposeAddComponent} from '../purpose-add/purpose-add.component';
import {ToastsManager} from 'ng2-toastr';
import {Marker} from '../../region/models/Marker';
import {Documentation} from "../../documentation/models/Documentation";
import {DocumentationService} from "../../documentation/documentation.service";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";
import {Project} from "../../project/models/Project";
import {ProjectPickerComponent} from "../../project/project-picker/project-picker.component";

@Component({
  selector: 'app-data-sharing-agreement-editor',
  templateUrl: './data-sharing-agreement-editor.component.html',
  styleUrls: ['./data-sharing-agreement-editor.component.css']
})
export class DataSharingAgreementEditorComponent implements OnInit {
  private paramSubscriber: any;

  dsa: Dsa = <Dsa>{};
  dataFlows: DataFlow[];
  regions: Region[];
  projects: Project[];
  publishers: Organisation[];
  subscribers: Organisation[];
  documentations: Documentation[];
  purposes: Purpose[] = [];
  benefits: Purpose[] = [];
  publisherMarkers: Marker[];
  subscriberMarkers: Marker[];
  mapMarkers: Marker[];
  showPub = true;
  allowEdit = false;
  file: File;
  pdfSrc: any;
  disableStatus = false;

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

  dataflowDetailsToShow = new DataFlow().getDisplayItems();
  regionDetailsToShow = new Region().getDisplayItems();
  orgDetailsToShow = new Organisation().getDisplayItems();
  purposeDetailsToShow = new Purpose().getDisplayItems();
  documentDetailsToShow = new Documentation().getDisplayItems();
  projectDetailsToShow = new Project().getDisplayItems();

  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private dsaService: DataSharingAgreementService,
              private securityService: SecurityService,
              private documentationService: DocumentationService,
              private router: Router,
              private route: ActivatedRoute,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.paramSubscriber = this.route.params.subscribe(
    params => {
      this.performAction(params['mode'], params['id']);
    });

    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
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
    const vm = this;
    console.log(uuid);
    vm.dsaService.getDsa(uuid)
      .subscribe(result =>  {
          vm.dsa = result;
          vm.checkEndDate();
          vm.getLinkedDataFlows();
          vm.getLinkedRegions();
          vm.getPublishers();
          vm.getProjects();
          vm.getSubscribers();
          vm.getPurposes();
          vm.getBenefits();
          vm.getPublisherMarkers();
          vm.getSubscriberMarkers();
          vm.getAssociatedDocumentation();
        },
        error => vm.log.error('The data sharing agreement could not be loaded. Please try again.', error, 'Load data sharing agreement')
      );
  }

  save(close: boolean) {
    const vm = this;
    console.log('before');
    console.log(vm.dsa);
    // Populate data flows before save
    vm.dsa.dataFlows = {};
    for (const idx in this.dataFlows) {
      const dataflow: DataFlow = this.dataFlows[idx];
      this.dsa.dataFlows[dataflow.uuid] = dataflow.name;
    }

    // Populate regions before save
    vm.dsa.regions = {};
    for (const idx in this.regions) {
      const region: Region = this.regions[idx];
      this.dsa.regions[region.uuid] = region.name;
    }

    // Populate publishers before save
    vm.dsa.publishers = {};
    for (const idx in this.publishers) {
      const pub: Organisation = this.publishers[idx];
      this.dsa.publishers[pub.uuid] = pub.name;
    }

    // Populate subscribers before save
    vm.dsa.subscribers = {};
    for (const idx in this.subscribers) {
      const sub: Organisation = this.subscribers[idx];
      this.dsa.subscribers[sub.uuid] = sub.name;
    }

    // Populate projects before save
    vm.dsa.projects = {};
    for (const idx in this.projects) {
      const proj: Project = this.projects[idx];
      this.dsa.projects[proj.uuid] = proj.name;
    }

    // Populate purposes before save
    vm.dsa.purposes = [];
    vm.dsa.purposes = this.purposes;

    // Populate benefits before save
    vm.dsa.benefits = [];
    vm.dsa.benefits = this.benefits;

    // Populate documents before save
    vm.dsa.documentations = [];
    vm.dsa.documentations = vm.documentations;

    console.log(vm.dsa);
    vm.dsaService.saveDsa(vm.dsa)
      .subscribe(saved => {
          vm.dsa.uuid = saved;
          vm.log.success('Data sharing agreement saved', vm.dsa, 'Save data sharing agreement');

          console.log('after');
          console.log(vm.dsa);
          if (close) { vm.close(); }
        },
        error => vm.log.error('The data sharing agreement could not be saved. Please try again.', error, 'Save data sharing agreement')
      );
  }

  close() {
    window.history.back();
  }

  private editDataFlows() {
    const vm = this;
    DataflowPickerComponent.open(vm.$modal, vm.dataFlows)
      .result.then(function
      (result: DataFlow[]) { vm.dataFlows = result; },
      () => vm.log.info('Edit data flows cancelled')
    );
  }

  private editRegions() {
    const vm = this;
    RegionPickerComponent.open(vm.$modal, vm.regions, '', 1)
      .result.then(function
      (result: Region[]) { vm.regions = result; },
      () => vm.log.info('Edit regions cancelled')
    );
  }

  private editProjects() {
    const vm = this;
    ProjectPickerComponent.open(vm.$modal, vm.projects)
      .result.then(function
      (result: Project[]) { vm.projects = result; },
      () => vm.log.info('Edit projects cancelled')
    );
  }


  private editPublishers() {
    const vm = this;
    if (!vm.regions[0]) {
      MessageBoxDialog.open(vm.$modal, 'Edit publishers', 'The data sharing agreement must be associated with a region before editing publishers', 'Ok', '')
        .result.then();
    } else {
      OrganisationPickerComponent.open(vm.$modal, vm.publishers, 'publisher', '', vm.regions[0].uuid, '')
        .result.then(function
        (result: Organisation[]) {
          vm.publishers = result;
        },
        () => vm.log.info('Edit publishers cancelled')
      );
    }
  }

  private editSubscribers() {
    const vm = this;
    if (!vm.regions[0]) {
      MessageBoxDialog.open(vm.$modal, 'Edit subscribers', 'The data sharing agreement must be associated with a region before editing subscribers', 'Ok', '')
        .result.then();
    } else {
      OrganisationPickerComponent.open(vm.$modal, vm.subscribers, 'subscriber', '', vm.regions[0].uuid, '')
        .result.then(function
        (result: Organisation[]) {
          vm.subscribers = result;
        },
        () => vm.log.info('Edit subscribers cancelled')
      );
    }
  }

  private editPurposes(index: number = -1) {
    const vm = this;
    PurposeAddComponent.open(vm.$modal, vm.purposes, 'Purpose', index)
      .result.then(function
      (result: Purpose[]) { vm.purposes = result; },
      () => vm.log.info('Edit purposes cancelled')
    );
  }

  private editBenefits(index: number = -1) {
    const vm = this;
    PurposeAddComponent.open(vm.$modal, vm.benefits, 'Benefit', index)
      .result.then(function
      (result: Purpose[]) { vm.benefits = result; },
      () => vm.log.info('Edit benefits cancelled')
    );
  }

  private editDataFlow(item: DataFlow) {
    this.router.navigate(['/dataFlow', item.uuid, 'edit']);
  }

  private getLinkedDataFlows() {
    const vm = this;
    vm.dsaService.getLinkedDataFlows(vm.dsa.uuid)
      .subscribe(
        result => vm.dataFlows = result,
        error => vm.log.error('The associated data flows could not be loaded. Please try again.', error, 'Load associated data flows')
      );
  }

  private getLinkedRegions() {
    const vm = this;
    vm.dsaService.getLinkedRegions(vm.dsa.uuid)
      .subscribe(
        result => vm.regions = result,
        error => vm.log.error('The associated regions could not be loaded. Please try again.', error, 'Load associated regions')
      );
  }

  private getPublishers() {
    const vm = this;
    vm.dsaService.getPublishers(vm.dsa.uuid)
      .subscribe(
        result => vm.publishers = result,
        error => vm.log.error('The associated publishers could not be loaded. Please try again.', error, 'Load associated publishers')
      );
  }

  private getProjects() {
    const vm = this;
    vm.dsaService.getProjects(vm.dsa.uuid)
      .subscribe(
        result => { vm.projects = result;
        console.log('projects', result); },
        error => vm.log.error('The associated projects could not be loaded. Please try again.', error, 'Load associated projects')
      );
  }

  private getSubscribers() {
    const vm = this;
    vm.dsaService.getSubscribers(vm.dsa.uuid)
      .subscribe(
        result => vm.subscribers = result,
        error => vm.log.error('The associated subscribers could not be loaded. Please try again.', error, 'Load associated subscribers')
      );
  }

  private getPurposes() {
    const vm = this;
    vm.dsaService.getPurposes(vm.dsa.uuid)
      .subscribe(
        result => vm.purposes = result,
        error => vm.log.error('The associated purposes could not be loaded. Please try again.', error, 'Load associated purposes')
      );
  }

  private getBenefits() {
    const vm = this;
    vm.dsaService.getBenefits(vm.dsa.uuid)
      .subscribe(
        result => vm.benefits = result,
        error => vm.log.error('The associated benefits could not be loaded. Please try again.', error, 'Load associated benefits')
      );
  }

  private getAssociatedDocumentation() {
    const vm = this;
    vm.documentationService.getAllAssociatedDocuments(vm.dsa.uuid, '3')
      .subscribe(
        result => vm.documentations = result,
        error => vm.log.error('The associated documentation could not be loaded. Please try again.', error, 'Load associated documentation')
      );
  }

  private getSubscriberMarkers() {
    const vm = this;
    vm.dsaService.getSubscriberMarkers(vm.dsa.uuid)
      .subscribe(
        result => {
          vm.subscriberMarkers = result;
        },
        error => vm.log.error('The associated subscriber map data could not be loaded. Please try again.', error, 'Load subscriber map data')
      )
  }

  private getPublisherMarkers() {
    const vm = this;
    vm.dsaService.getPublisherMarkers(vm.dsa.uuid)
      .subscribe(
        result => {
          vm.mapMarkers = result;
          vm.publisherMarkers = result;
          console.log(vm.publisherMarkers);
        },
        error => vm.log.error('The associated publisher map data could not be loaded. Please try again.', error, 'Load publisher map data')
      )
  }

  clickOnPurpose($event) {
    let index = this.purposes.indexOf($event, 0);
    this.editPurposes(index);
  }

  clickOnBenefit($event) {
    let index = this.benefits.indexOf($event, 0);
    this.editBenefits(index);
  }

  removeFromPurposes(match: Purpose) {
    const index = this.purposes.indexOf(match, 0);
    if (index > -1) {
      this.purposes.splice(index, 1);
    }
  }

  removeFromBenefits(match: Purpose) {
    const index = this.benefits.indexOf(match, 0);
    if (index > -1) {
      this.benefits.splice(index, 1);
    }
  }

  removeFromDocumentation(match: Documentation) {
    const index = this.documentations.indexOf(match, 0);
    if (index > -1) {
      this.documentations.splice(index, 1);
    }
  }

  swapMarkers() {
    const vm = this;
    console.log(vm.showPub);
    if (vm.showPub) {
      console.log('showing pubs');
      vm.mapMarkers = vm.publisherMarkers;
    } else {
      console.log('showing subs');
      vm.mapMarkers = vm.subscriberMarkers;
    }
  }

  private uploadFile() {
    const vm = this;
    const myReader: FileReader = new FileReader();

    myReader.onloadend = function(e){
      // you can perform an action with readed data here
      vm.log.success('Uploading document', null, 'Upload document');
      vm.pdfSrc = myReader.result;
      const newDoc: Documentation = new Documentation();
      newDoc.fileData = myReader.result;
      newDoc.title = vm.file.name;
      newDoc.filename = vm.file.name;
      vm.documentations.push(newDoc);
    }


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
    const vm = this;
    vm.file = null;
  }

  checkEndDate() {
    const vm = this;

    if (vm.dsa.endDate === null) {
      vm.disableStatus = false;
      return;
    }

    var today = new Date();
    today.setHours(0,0,0,0);
    var endDate = new Date(vm.dsa.endDate)

    if (endDate < today) {
      vm.dsa.dsaStatusId = 1;
      vm.disableStatus = true;
    } else {
      vm.disableStatus = false;
    }
  }

}
