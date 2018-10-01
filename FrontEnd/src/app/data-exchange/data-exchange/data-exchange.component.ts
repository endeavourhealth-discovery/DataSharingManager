import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataExchangeService} from '../data-exchange.service';
import {DataExchange} from '../models/DataExchange';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {Router} from '@angular/router';
import {ToastsManager} from 'ng2-toastr';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-data-exchange',
  templateUrl: './data-exchange.component.html',
  styleUrls: ['./data-exchange.component.css']
})
export class DataExchangeComponent implements OnInit {
  exchanges: DataExchange[];
  allowEdit = false;
  loadingComplete = false;

  public activeProject: UserProject;

  dataExchangeDetailsToShow = new DataExchange().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dataExchangeService: DataExchangeService,
              private securityService: SecurityService,
              private log: LoggerService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
    this.getDataExchanges();
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
  }

  getDataExchanges() {
    const vm = this;
    vm.loadingComplete = false;
    vm.dataExchangeService.getAllDataExchanges()
      .subscribe(
        result => {
          vm.exchanges = result;
          vm.loadingComplete = true;
        },
            error => {
          vm.log.error('The data exchanges could not be loaded. Please try again.', error, 'Load data exchanges');
          vm.loadingComplete = true;
        }
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
    MessageBoxDialog.open(vm.$modal, 'Delete data exchange', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data exchange', 'Cancel')
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
        (error) => vm.log.error('The data exchange could not be deleted. Please try again.', error, 'Delete data exchange')
      );
  }

  close() {
    this.router.navigate(['/sharingOverview']);
  }

}
