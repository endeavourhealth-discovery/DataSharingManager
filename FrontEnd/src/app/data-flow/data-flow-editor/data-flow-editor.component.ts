import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataFlowService} from '../data-flow.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataFlow} from '../models/DataFlow';
import {DataSharingAgreementPickerComponent} from '../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component';
import {DataProcessingAgreementPickerComponent} from '../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component';
import {Dsa} from '../../data-sharing-agreement/models/Dsa';
import {Dpa} from '../../data-processing-agreement/models/Dpa';
import {LoggerService, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastsManager} from "ng2-toastr";
import {Documentation} from "../../documentation/models/Documentation";
import {DocumentationService} from "../../documentation/documentation.service";
import {DataExchange} from "../../data-exchange/models/DataExchange";
import {Organisation} from "../../organisation/models/Organisation";
import {DataExchangePickerComponent} from "../../data-exchange/data-exchange-picker/data-exchange-picker.component";
import {OrganisationPickerComponent} from "../../organisation/organisation-picker/organisation-picker.component";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-data-flow-editor',
  templateUrl: './data-flow-editor.component.html',
  styleUrls: ['./data-flow-editor.component.css']
})
export class DataFlowEditorComponent implements OnInit {
  private paramSubscriber: any;
  public accordionClass = 'accordionClass';

  dataFlow: DataFlow = <DataFlow>{};
  dsas: Dsa[];
  dpas: Dpa[];
  exchanges: DataExchange[];
  publishers: Organisation[];
  subscribers: Organisation[];
  documentations: Documentation[];
  allowEdit = false;
  file: File;
  pdfSrc: any;

  public activeProject: UserProject;

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

  dsaDetailsToShow = new Dsa().getDisplayItems();
  dpaDetailsToShow = new Dpa().getDisplayItems();
  exchangeDetailsToShow = new DataExchange().getDisplayItems();
  OrganisationDetailsToShow = new Organisation().getDisplayItems();
  documentDetailsToShow = new Documentation().getDisplayItems();


  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private dataFlowService: DataFlowService,
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
    this.dataFlow = {
      name : ''
    } as DataFlow;
  }

  load(uuid: string) {
    const vm = this;
    vm.dataFlowService.getDataFlow(uuid)
      .subscribe(result =>  {
          vm.dataFlow = result;
          vm.getLinkedDpas();
          vm.getLinkedDsas();
          vm.getLinkedExchanges();
          vm.getLinkedPublishers();
          vm.getLinkedSubscribers();
          vm.getAssociatedDocumentation();
        },
        error => vm.log.error('The data flow could not be loaded. Please try again.', error, 'Load data flow')
      );
  }

  save(close: boolean) {
    const vm = this;
    // Populate Data Sharing Agreements before save
    vm.dataFlow.dsas = {};
    for (let idx in this.dsas) {
      const dsa: Dsa = this.dsas[idx];
      this.dataFlow.dsas[dsa.uuid] = dsa.name;
    }

    // Populate Data Processing Agreements before save
    vm.dataFlow.dpas = {};
    for (let idx in this.dpas) {
      const dpa: Dpa = this.dpas[idx];
      this.dataFlow.dpas[dpa.uuid] = dpa.name;
    }

    // Populate exchanges before save
    vm.dataFlow.exchanges = {};
    for (let idx in this.exchanges) {
      const exchange: DataExchange = this.exchanges[idx];
      this.dataFlow.exchanges[exchange.uuid] = exchange.name;
    }

    // Populate publishers before save
    vm.dataFlow.publishers = {};
    for (let idx in this.publishers) {
      const pub: Organisation = this.publishers[idx];
      this.dataFlow.publishers[pub.uuid] = pub.name;
    }

    // Populate subscribers before save
    vm.dataFlow.subscribers = {};
    for (let idx in this.subscribers) {
      const sub: Organisation = this.subscribers[idx];
      this.dataFlow.subscribers[sub.uuid] = sub.name;
    }

    // Populate documents before save
    vm.dataFlow.documentations = [];
    vm.dataFlow.documentations = vm.documentations;

    vm.dataFlowService.saveDataFlow(vm.dataFlow)
      .subscribe(saved => {
          vm.dataFlow.uuid = saved;
          vm.log.success('Data flow saved', vm.dataFlow, 'Save data flow');
          if (close) { vm.close(); }
        },
        error => vm.log.error('The data flow could not be saved. Please try again.', error, 'Save data flow')
      );
  }

  close() {
    window.history.back();
  }

  private editDataSharingAgreements() {
    const vm = this;
    DataSharingAgreementPickerComponent.open(vm.$modal, vm.dsas)
      .result.then(function
      (result: Dsa[]) { vm.dsas = result; },
      () => vm.log.info('Edit data sharing agreements cancelled')
    );
  }

  private editDataProcessingAgreements() {
    const vm = this;
    DataProcessingAgreementPickerComponent.open(vm.$modal, vm.dpas)
      .result.then(function
      (result: Dpa[]) { vm.dpas = result; },
      () => vm.log.info('Edit data processing agreements cancelled')
    );
  }

  private editDataExchanges() {
    const vm = this;
    DataExchangePickerComponent.open(vm.$modal, vm.exchanges)
      .result.then(function
      (result: DataExchange[]) { vm.exchanges = result; },
      () => vm.log.info('Edit data exchanges cancelled')
    );
  }

  private editPublishers() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.publishers, "organisations")
      .result.then(function
      (result: Organisation[]) { vm.publishers = result; },
      () => vm.log.info('Edit publishers cancelled')
    );
  }

  private editSubscribers() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.subscribers, "organisations")
      .result.then(function
      (result: Organisation[]) { vm.subscribers = result; },
      () => vm.log.info('Edit subscribers cancelled')
    );
  }

  private getLinkedDpas() {
    const vm = this;
    vm.dataFlowService.getLinkedDpas(vm.dataFlow.uuid)
      .subscribe(
        result => vm.dpas = result,
        error => vm.log.error('The associated data processing agreements could not be loaded. Please try again.', error, 'Load associated data processing agreements')
      );
  }

  private getLinkedDsas() {
    const vm = this;
    vm.dataFlowService.getLinkedDsas(vm.dataFlow.uuid)
      .subscribe(
        result => vm.dsas = result,
        error => vm.log.error('The associated data sharing agreements could not be loaded. Please try again.', error, 'Load associated data sharing agreements')
      );
  }

  private getLinkedExchanges() {
    const vm = this;
    vm.dataFlowService.getLinkedExchanges(vm.dataFlow.uuid)
      .subscribe(
        result => vm.exchanges = result,
        error => vm.log.error('The associated data exchanges could not be loaded. Please try again.', error, 'Load associated data exchanges')
      );
  }

  private getLinkedPublishers() {
    const vm = this;
    vm.dataFlowService.getLinkedPublishers(vm.dataFlow.uuid)
      .subscribe(
        result => vm.publishers = result,
        error => vm.log.error('The associated publishers could not be loaded. Please try again.', error, 'Load associated publishers')
      );
  }

  private getLinkedSubscribers() {
    const vm = this;
    vm.dataFlowService.getLinkedSubscribers(vm.dataFlow.uuid)
      .subscribe(
        result => vm.subscribers = result,
        error => vm.log.error('The associated subscribers could not be loaded. Please try again.', error, 'Load associated subscribers')
      );
  }

  private getAssociatedDocumentation() {
    const vm = this;
    vm.documentationService.getAllAssociatedDocuments(vm.dataFlow.uuid, '4')
      .subscribe(
        result => vm.documentations = result,
        error => vm.log.error('The associated documentation could not be loaded. Please try again.', error, 'Load associated documentation')
      );
  }

  removeFromDocumentation(match: Documentation) {
    const index = this.documentations.indexOf(match, 0);
    if (index > -1) {
      this.documentations.splice(index, 1);
    }
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

  private uploadFile() {
    const vm = this;
    const myReader: FileReader = new FileReader();

    myReader.onloadend = function(e){
      // you can perform an action with readed data here
      vm.log.success('Uploading complete', null, 'Upload document');
      vm.pdfSrc = myReader.result;
      const newDoc: Documentation = new Documentation();
      newDoc.fileData = myReader.result;
      newDoc.title = vm.file.name;
      newDoc.filename = vm.file.name;
      vm.documentations.push(newDoc);
    }


    myReader.readAsDataURL(vm.file);
  }

}
