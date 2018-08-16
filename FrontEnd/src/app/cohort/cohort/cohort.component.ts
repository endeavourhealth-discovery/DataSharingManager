import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {CohortService} from '../cohort.service';
import {LoggerService, MessageBoxDialog, SecurityService} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Cohort} from '../models/Cohort';
import {ToastsManager} from 'ng2-toastr';

@Component({
  selector: 'app-cohort',
  templateUrl: './cohort.component.html',
  styleUrls: ['./cohort.component.css']
})
export class CohortComponent implements OnInit {
  cohorts: Cohort[];
  pageSize = 20;
  allowEdit = false;
  cohortDetailsToShow = new Cohort().getDisplayItems();
  loadingComplete = false;

  constructor(private $modal: NgbModal,
              private cohortService: CohortService,
              private securityService: SecurityService,
              private log: LoggerService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.checkEditPermission();
    this.getCohorts();
  }

  checkEditPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:admin'))
      vm.allowEdit = true;
  }

  getCohorts() {
    const vm = this;
    vm.loadingComplete = false;
    vm.cohortService.getAllCohorts()
      .subscribe(
        result => {
          vm.cohorts = result;
          vm.loadingComplete = true;
        },
            error => {
          vm.log.error('Failed to load cohorts', error, 'Load cohorts');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/cohort', 1, 'add']);
  }

  edit(item: Cohort) {
    this.router.navigate(['/cohort', item.uuid,  'edit']);
  }

  delete(item: Cohort) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete cohort', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete cohort', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Cohort) {
    const vm = this;
    vm.cohortService.deleteCohort(item.uuid)
      .subscribe(
        () => {
          const index = vm.cohorts.indexOf(item);
          vm.cohorts.splice(index, 1);
          vm.log.success('Cohort deleted', item, 'Delete Cohort');
        },
        (error) => vm.log.error('Failed to delete Cohort', error, 'Delete Cohort')
      );
  }

  close() {
    this.router.navigate(['/sharingOverview']);
  }

}
