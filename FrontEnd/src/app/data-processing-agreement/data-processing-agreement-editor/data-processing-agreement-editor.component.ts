import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataProcessingAgreementService} from '../data-processing-agreement.service';
import {LoggerService, SecurityService} from 'eds-angular4';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataSet} from '../../data-set/models/Dataset';
import {DataFlow} from '../../data-flow/models/DataFlow';
import {Dpa} from '../models/Dpa';
import {DataflowPickerComponent} from '../../data-flow/dataflow-picker/dataflow-picker.component';
import {Cohort} from '../../cohort/models/Cohort';
import {Documentation} from '../../documentation/models/Documentation';
import {CohortPickerComponent} from '../../cohort/cohort-picker/cohort-picker.component';
import {DataSetPickerComponent} from '../../data-set/data-set-picker/data-set-picker.component';
import {DocumentationService} from '../../documentation/documentation.service';
import {ToastsManager} from 'ng2-toastr';
import {Organisation} from '../../organisation/models/Organisation';
import {OrganisationPickerComponent} from '../../organisation/organisation-picker/organisation-picker.component';
import {Marker} from '../../region/models/Marker';
import {Purpose} from '../../data-sharing-agreement/models/Purpose';
import {PurposeAddComponent} from '../../data-sharing-agreement/purpose-add/purpose-add.component';

@Component({
  selector: 'app-data-processing-agreement-editor',
  templateUrl: './data-processing-agreement-editor.component.html',
  styleUrls: ['./data-processing-agreement-editor.component.css']
})
export class DataProcessingAgreementEditorComponent implements OnInit {
  private paramSubscriber: any;
  public accordionClass = 'accordionClass';

  dpa: Dpa = <Dpa>{};
  dataFlows: DataFlow[];
  cohorts: Cohort[];
  dataSets: DataSet[];
  documentations: Documentation[];
  publishers: Organisation[];
  allowEdit = false;
  processor = 'Discovery';
  file: File;
  pdfSrc: any;
  page = 1;
  publisherMarkers: Marker[];
  subscriberMarkers: Marker[];
  mapMarkers: Marker[];
  purposes: Purpose[] = [];
  benefits: Purpose[] = [];
  disableStatus = false;

  status = [
    {num: 0, name: 'Active'},
    {num: 1, name: 'Inactive'}
  ];

  dataFlowDetailsToShow = new DataFlow().getDisplayItems();
  datasetDetailsToShow = new DataSet().getDisplayItems();
  cohortDetailsToShow = new Cohort().getDisplayItems();
  documentDetailsToShow = new Documentation().getDisplayItems();
  orgDetailsToShow = new Organisation().getDisplayItems();
  purposeDetailsToShow = new Purpose().getDisplayItems();

  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private dpaService: DataProcessingAgreementService,
              private documentationService: DocumentationService,
              private securityService: SecurityService,
              private router: Router,
              private route: ActivatedRoute,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.checkEditPermission();
    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode'], params['id']);
      });
  }

  checkEditPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:admin'))
      vm.allowEdit = true;
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
      name: ''
    } as Dpa;
  }

  load(uuid: string) {
    const vm = this;
    console.log(uuid);
    vm.dpaService.getDpa(uuid)
      .subscribe(result => {
          vm.dpa = result;
          vm.checkEndDate();
          vm.getPublishers();
          vm.getLinkedDataFlows();
          vm.getLinkedCohorts();
          vm.getLinkedDataSets();
          vm.getAssociatedDocumentation();
          vm.getPublisherMarkers();
          vm.getSubscriberMarkers()
          vm.getPurposes();
          vm.getBenefits();
        },
        error => vm.log.error('The data processing agreement could not be loaded. Please try again.', error, 'Load data processing agreement')
      );
  }

  save(close: boolean) {
    const vm = this;

    // Populate Data Flows before save
    vm.dpa.dataFlows = {};
    for (const idx in this.dataFlows) {
      const dataflow: DataFlow = this.dataFlows[idx];
      this.dpa.dataFlows[dataflow.uuid] = dataflow.name;
    }

    // Populate publishers before save
    vm.dpa.publishers = {};
    for (const idx in this.publishers) {
      const pub: Organisation = this.publishers[idx];
      this.dpa.publishers[pub.uuid] = pub.name;
    }

    // Populate Cohorts before save
    vm.dpa.cohorts = {};
    for (const idx in this.cohorts) {
      const cohort: Cohort = this.cohorts[idx];
      this.dpa.cohorts[cohort.uuid] = cohort.name;
    }

    // Populate DataSets before save
    vm.dpa.dataSets = {};
    for (const idx in this.dataSets) {
      const dataset: DataSet = this.dataSets[idx];
      this.dpa.dataSets[dataset.uuid] = dataset.name;
    }

    // Populate purposes before save
    vm.dpa.purposes = [];
    vm.dpa.purposes = this.purposes;

    // Populate benefits before save
    vm.dpa.benefits = [];
    vm.dpa.benefits = this.benefits;

    // Populate documents before save
    vm.dpa.documentations = [];
    vm.dpa.documentations = vm.documentations;

    vm.dpaService.saveDpa(vm.dpa)
      .subscribe(saved => {
          vm.dpa.uuid = saved;
          vm.log.success('Data processing agreement saved', vm.dpa, 'Save data processing agreement');
          if (close) { vm.close(); }
        },
        error => vm.log.error('The data processing agreement could not be saved. Please try again.', error, 'Save data processing agreement')
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

  private editCohorts() {
    const vm = this;
    CohortPickerComponent.open(vm.$modal, vm.cohorts)
      .result.then(function
      (result: Cohort[]) { vm.cohorts = result; },
      () => vm.log.info('Edit cohorts cancelled')
    );
  }

  private editDataSets() {
    const vm = this;
    DataSetPickerComponent.open(vm.$modal, vm.dataSets)
      .result.then(function
      (result: DataSet[]) { vm.dataSets = result; },
      () => vm.log.info('Edit data sets cancelled')
    );
  }

  private editDataFlow(item: DataFlow) {
    this.router.navigate(['/dataFlow', item.uuid, 'edit']);
  }

  private editCohort(item: Cohort) {
    this.router.navigate(['/cohort', item.uuid, 'edit']);
  }

  private editDataSet(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
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

  private getLinkedCohorts() {
    const vm = this;
    vm.dpaService.getLinkedCohorts(vm.dpa.uuid)
      .subscribe(
        result => vm.cohorts = result,
        error => vm.log.error('The associated cohorts could not be loaded. Please try again.', error, 'Load associated cohorts')
      );
  }

  private getLinkedDataFlows() {
    const vm = this;
    vm.dpaService.getLinkedDataFlows(vm.dpa.uuid)
      .subscribe(
        result => vm.dataFlows = result,
        error => vm.log.error('The associated data flows could not be loaded. Please try again.', error, 'Load asscoiated data flows')
      );
  }

  private getLinkedDataSets() {
    const vm = this;
    vm.dpaService.getLinkedDataSets(vm.dpa.uuid)
      .subscribe(
        result => vm.dataSets = result,
        error => vm.log.error('The associated data sets could not be loaded. Please try again.', error, 'Load associated data sets')
      );
  }

  private getPurposes() {
    const vm = this;
    vm.dpaService.getPurposes(vm.dpa.uuid)
      .subscribe(
        result => vm.purposes = result,
        error => vm.log.error('The associated purposes could not be loaded. Please try again.', error, 'Load purposes')
      );
  }

  private getBenefits() {
    const vm = this;
    vm.dpaService.getBenefits(vm.dpa.uuid)
      .subscribe(
        result => vm.benefits = result,
        error => vm.log.error('The associated benefits could not be loaded. Please try again.', error, 'Load benefits')
      );
  }

  private getAssociatedDocumentation() {
    const vm = this;
    vm.documentationService.getAllAssociatedDocuments(vm.dpa.uuid, '5')
      .subscribe(
        result => vm.documentations = result,
        error => vm.log.error('The associated documentation could not be loaded. Please try again.', error, 'Load associated documentation')
      );
  }

  fileChange(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file = fileList[0];
    } else {
      this.file = null;
    }
  }

  private uploadFile() {
    const vm = this;
    const myReader: FileReader = new FileReader();

    myReader.onloadend = function(e){
      // you can perform an action with readed data here
      vm.log.success('Uploading file', null, 'Upload document');
      vm.pdfSrc = myReader.result;
      const newDoc: Documentation = new Documentation();
      newDoc.fileData = myReader.result;
      newDoc.title = vm.file.name;
      newDoc.filename = vm.file.name;
      vm.documentations.push(newDoc);
    }


    myReader.readAsDataURL(vm.file);
  }

  ok() {
    this.uploadFile();
  }

  cancel() {
    this.file = null;
  }

  delete($event) {
    console.log($event);
  }

  private editPublishers() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.publishers, 'organisation')
      .result.then(function
      (result: Organisation[]) { vm.publishers = result; },
      () => vm.log.info('Edit publishers cancelled')
    );
  }

  private getPublishers() {
    const vm = this;
    vm.dpaService.getPublishers(vm.dpa.uuid)
      .subscribe(
        result => vm.publishers = result,
        error => vm.log.error('The associated publishers could not be loaded. Please try again.', error, 'Load publishers')
      );
  }

  private getSubscriberMarkers() {
    const vm = this;
    vm.dpaService.getSubscriberMarkers(vm.dpa.uuid)
      .subscribe(
        result => {
          vm.subscriberMarkers = result;
        },
        error => vm.log.error('The subscriber map date could not be loaded. Please try again.', error, 'Load subscriber map data')
      )
  }

  private getPublisherMarkers() {
    const vm = this;
    vm.dpaService.getPublisherMarkers(vm.dpa.uuid)
      .subscribe(
        result => {
          vm.publisherMarkers = result;
        },
        error => vm.log.error('The publisher map date could not be loaded. Please try again.', error, 'Load publisher map data')
      )
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

  clickOnPurpose($event) {
    let index = this.purposes.indexOf($event, 0);
    this.editPurposes(index);
  }

  clickOnBenefit($event) {
    let index = this.benefits.indexOf($event, 0);
    this.editBenefits(index);
  }

  checkEndDate() {
    const vm = this;

    if (vm.dpa.endDate === null) {
      vm.disableStatus = false;
      return;
    }

    var today = new Date();
    today.setHours(0,0,0,0);
    var endDate = new Date(vm.dpa.endDate);

    if (endDate < today) {
      vm.dpa.dsaStatusId = 1;
      vm.disableStatus = true;
    } else {
      vm.disableStatus = false;
    }
  }

}
