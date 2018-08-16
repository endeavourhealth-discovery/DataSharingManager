import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataSetService} from '../data-set.service';
import {LoggerService, MessageBoxDialog, SecurityService} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataSet} from '../models/Dataset';
import {ToastsManager} from 'ng2-toastr';

@Component({
  selector: 'app-data-set',
  templateUrl: './data-set.component.html',
  styleUrls: ['./data-set.component.css']
})
export class DataSetComponent implements OnInit {
  datasets: DataSet[];
  allowEdit = false;
  loadingComplete = false;

  datasetDetailsToShow = new DataSet().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dataSetService: DataSetService,
              private securityService: SecurityService,
              private log: LoggerService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.checkEditPermission();
    this.getDataSets();
  }

  checkEditPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:admin'))
      vm.allowEdit = true;
  }

  getDataSets() {
    const vm = this;
    vm.loadingComplete = false;
    vm.dataSetService.getAllDataSets()
      .subscribe(
        result => {
          vm.datasets = result;
          vm.loadingComplete = true;
        },
          error => {
          vm.log.error('Failed to load data sets', error, 'Load data sets');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dataSet', 1, 'add']);
  }

  edit(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
  }

  delete(item: DataSet) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete data set', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data set', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: DataSet) {
    const vm = this;
    vm.dataSetService.deleteDataSet(item.uuid)
      .subscribe(
        () => {
          const index = vm.datasets.indexOf(item);
          vm.datasets.splice(index, 1);
          vm.log.success('Data set deleted', item, 'Delete Data set');
        },
        (error) => vm.log.error('Failed to delete Data set', error, 'Delete Data flow')
      );
  }

  close() {
    this.router.navigate(['sharingOverview']);
  }

}
