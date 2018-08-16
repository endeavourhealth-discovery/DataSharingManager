import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {Dsa} from '../models/Dsa';
import {LoggerService, MessageBoxDialog, SecurityService} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
import {ToastsManager} from 'ng2-toastr';

@Component({
  selector: 'app-data-sharing-agreement',
  templateUrl: './data-sharing-agreement.component.html',
  styleUrls: ['./data-sharing-agreement.component.css']
})
export class DataSharingAgreementComponent implements OnInit {
  dsas: Dsa[];
  allowEdit = false;
  loadingComplete = false;

  dsaDetailsToShow = new Dsa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dsaService: DataSharingAgreementService,
              private securityService: SecurityService,
              private log: LoggerService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.checkEditPermission();
    this.getDsas();
  }

  checkEditPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:admin'))
      vm.allowEdit = true;
  }

  getDsas() {
    const vm = this;
    vm.loadingComplete = false;
    vm.dsaService.getAllDsas()
      .subscribe(
        result => {
          vm.dsas = result;
          vm.loadingComplete = true;
        },
        error => {
          vm.log.error('The data sharing agreements could not be loaded. Please try again.', error, 'Load data sharing agreements');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dsa', 1, 'add']);
  }

  edit(item: Dsa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  delete(item: Dsa) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete data sharing agreement', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data sharing agreement', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Dsa) {
    const vm = this;
    vm.dsaService.deleteDsa(item.uuid)
      .subscribe(
        () => {
          const index = vm.dsas.indexOf(item);
          vm.dsas.splice(index, 1);
          vm.log.success('Data sharing agreement deleted', item, 'Delete data sharing agreement');
        },
        (error) => vm.log.error('The data sharing agreement could not be deleted. Please try again.', error, 'Delete data sharing agreement')
      );
  }

  close() {
    this.router.navigate(['/sharingOverview']);
  }

}
