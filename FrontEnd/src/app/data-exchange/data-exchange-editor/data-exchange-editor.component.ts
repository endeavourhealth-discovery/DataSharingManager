import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataExchangeService} from '../data-exchange.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataExchange} from '../models/DataExchange';
import {LoggerService, SecurityService} from 'eds-angular4';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastsManager} from "ng2-toastr";
import {DataFlow} from "../../data-flow/models/DataFlow";
import {DataflowPickerComponent} from "../../data-flow/dataflow-picker/dataflow-picker.component";

@Component({
  selector: 'app-data-exchange-editor',
  templateUrl: './data-exchange-editor.component.html',
  styleUrls: ['./data-exchange-editor.component.css']
})
export class DataExchangeEditorComponent implements OnInit {
  private paramSubscriber: any;
  public accordionClass = 'accordionClass';

  exchange: DataExchange = <DataExchange>{};
  dataFlows: DataFlow[];
  allowEdit = false;

  flowDirections = [
    {num: 0, name : 'Inbound'},
    {num: 1, name : 'Outbound'}
  ];

  flowSchedules = [
    {num: 0, name : 'Daily'},
    {num: 1, name : 'On Demand'}
  ];

  exchangeMethod = [
    {num: 0, name : 'Paper'},
    {num: 1, name : 'Electronic'}
  ];

  flowStatus = [
    {num: 0, name : 'In Development'},
    {num: 1, name : 'Live'}
  ];

  storageProtocols = [
    {num: 0, name: 'Audit only'},
    {num: 1, name: 'Temporary Store And Forward'},
    {num: 2, name: 'Permanent Record Store'}
  ];

  securityArchitectures = [
    {num: 0, name: 'TLS/MA'},
    {num: 1, name: 'Secure FTP'}
  ];

  securityInfrastructures = [
    {num: 0, name: 'N3'},
    {num: 1, name: 'PSN'},
    {num: 2, name: 'Internet'}
  ];

  publisherSubscriber = [
    {num: 0, name: 'Subscriber'},
    {num: 1, name: 'Publisher'}
  ];

  dataflowDetailsToShow = new DataFlow().getDisplayItems();

  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private dataExchangeService: DataExchangeService,
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
    this.exchange = {
      name : ''
    } as DataExchange;
  }

  load(uuid: string) {
    const vm = this;
    vm.dataExchangeService.getDataExchange(uuid)
      .subscribe(result =>  {
          vm.exchange = result;
          vm.getLinkedDataFlows();
        },
        error => vm.log.error('Error loading', error, 'Error')
      );
  }

  save(close: boolean) {
    const vm = this;
    // Populate Data flows before save
    vm.exchange.dataFlows = {};
    for (let idx in this.dataFlows) {
      const dataFlow: DataFlow = this.dataFlows[idx];
      this.exchange.dataFlows[dataFlow.uuid] = dataFlow.name;
    }

    vm.dataExchangeService.saveDataExchange(vm.exchange)
      .subscribe(saved => {
          vm.exchange.uuid = saved;
          vm.log.success('Data exchange saved', vm.exchange, 'Saved');
          if (close) { vm.close(); }
        },
        error => vm.log.error('Error saving data exchange', error, 'Error')
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

  private getLinkedDataFlows() {
    const vm = this;
    vm.dataExchangeService.getLinkedDataFlows(vm.exchange.uuid)
      .subscribe(
        result => vm.dataFlows = result,
        error => vm.log.error('Failed to load linked data flows', error, 'Load linked data flows')
      );
  }
}
