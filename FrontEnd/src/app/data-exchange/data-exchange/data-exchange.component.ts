import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataExchangeService} from '../data-exchange.service';
import {DataExchange} from '../models/DataExchange';
import {LoggerService, MessageBoxDialog, SecurityService} from 'eds-angular4';
import {Router} from '@angular/router';
import {ToastsManager} from 'ng2-toastr';

@Component({
  selector: 'app-data-exchange',
  templateUrl: './data-exchange.component.html',
  styleUrls: ['./data-exchange.component.css']
})
export class DataExchangeComponent implements OnInit {
  exchanges: DataExchange[] = [];
  allowEdit = false;

  dataExchangeDetailsToShow = new DataExchange().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dataExchangeService: DataExchangeService,
              private securityService: SecurityService,
              private log: LoggerService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.checkEditPermission();
    this.getDataExchanges();
  }

  checkEditPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:admin'))
      vm.allowEdit = true;
  }

  getDataExchanges() {

    const vm = this;
    vm.dataExchangeService.getAllDataExchanges()
      .subscribe(
        result => vm.exchanges = result,
        error => vm.log.error('Failed to load data exchanges', error, 'Load data exchanges')
      );
  }

  add() {
    this.router.navigate(['/dataExchange', 1, 'add']);
  }

  edit(item: DataExchange) {
    this.router.navigate(['/dataExchange', item.uuid, 'edit']);
  }

  delete(item: DataExchange) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete data exchange', 'Are you sure you want to delete the data exchange?', 'Yes', 'No')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: DataExchange) {
    const vm = this;
    vm.dataExchangeService.deleteDataExchange(item.uuid)
      .subscribe(
        () => {
          const index = vm.exchanges.indexOf(item);
          vm.exchanges.splice(index, 1);
          vm.log.success('Data exchange deleted', item, 'Delete data exchange');
        },
        (error) => vm.log.error('Failed to delete data exchange', error, 'Delete data exchange')
      );
  }

  close() {
    this.router.navigate(['/sharingOverview']);
  }

}
