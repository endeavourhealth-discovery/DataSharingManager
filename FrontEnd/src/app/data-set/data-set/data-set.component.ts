import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataSetService} from '../data-set.service';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataSet} from '../models/Dataset';
import {ToastsManager} from 'ng2-toastr';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-data-set',
  templateUrl: './data-set.component.html',
  styleUrls: ['./data-set.component.css']
})
export class DataSetComponent implements OnInit {
  datasets: DataSet[];
  allowEdit = false;
  loadingComplete = false;

  public activeProject: UserProject;

  datasetDetailsToShow = new DataSet().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dataSetService: DataSetService,
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

    this.getDataSets();
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
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
          vm.log.error('The data sets could not be loaded. Please try again.', error, 'Load data sets');
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
          vm.log.success('Data set deleted', item, 'Delete data set');
        },
        (error) => vm.log.error('The data set could not be deleted. Please try again.', error, 'Delete data set')
      );
  }

  close() {
    this.router.navigate(['overview']);
  }

}
