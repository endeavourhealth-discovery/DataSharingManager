import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataProcessingAgreementService} from '../data-processing-agreement.service';
import {LoggerService, MessageBoxDialog, SecurityService} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Dpa} from '../models/Dpa';
import {ToastsManager} from 'ng2-toastr';

@Component({
  selector: 'app-data-processing-agreement',
  templateUrl: './data-processing-agreement.component.html',
  styleUrls: ['./data-processing-agreement.component.css']
})
export class DataProcessingAgreementComponent implements OnInit {
  dpas: Dpa[];
  allowEdit = false;
  loadingComplete = false;

  dpaDetailsToShow = new Dpa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dpaService: DataProcessingAgreementService,
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
    vm.dpaService.getAllDpas()
      .subscribe(
        result => {
          vm.dpas = result;
          vm.loadingComplete = true;
        },
            error => {
          vm.log.error('Failed to load dpas', error, 'Load dpa');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dpa', 1, 'add']);
  }

  edit(item: Dpa) {
    this.router.navigate(['/dpa', item.uuid, 'edit']);
  }

  delete(item: Dpa) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete Data Processing Agreement',
      'Are you sure you want to delete the Data Processing Agreement?', 'Yes', 'No')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Dpa) {
    const vm = this;
    vm.dpaService.deleteDpa(item.uuid)
      .subscribe(
        () => {
          const index = vm.dpas.indexOf(item);
          vm.dpas.splice(index, 1);
          vm.log.success('Data Sharing Processing deleted', item, 'Delete Data Processing Agreement');
        },
        (error) => vm.log.error('Failed to delete Data Processing Agreement', error, 'Delete Data Processing Agreement')
      );
  }

  close() {
    this.router.navigate(['/sharingOverview']);
  }

}
