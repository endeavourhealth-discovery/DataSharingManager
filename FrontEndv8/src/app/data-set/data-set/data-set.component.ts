import {Component, OnInit} from '@angular/core';
import {DataSet} from '../models/Dataset';
import {DataSetService} from '../data-set.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";

@Component({
  selector: 'app-data-set',
  templateUrl: './data-set.component.html',
  styleUrls: ['./data-set.component.css']
})
export class DataSetComponent implements OnInit {
  datasets: DataSet[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  datasetDetailsToShow = new DataSet().getDisplayItems();
  loadingComplete = false;

  public activeProject: UserProject;

  constructor(private dataSetService: DataSetService,
              private router: Router,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

    this.getDataSets();
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
    }
  }

  getDataSets() {
    this.loadingComplete = false;
    this.dataSetService.getAllDataSets()
      .subscribe(
        result => {
          this.datasets = result;
          this.loadingComplete = true;
        },
          error => {
          this.log.error('The data sets could not be loaded. Please try again.'/*, error, 'Load data sets'*/);
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dataSet', 1, 'add']);
  }

  edit(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
  }

  itemClicked(dataset: DataSet) {
    this.router.navigate(['/dataSet', dataset.uuid, 'edit']);
  }

  delete(item: DataSet) {
    /*MessageBoxDialog.open(this.$modal, 'Delete data set', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data set', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: DataSet) {
    this.dataSetService.deleteDataSet(item.uuid)
      .subscribe(
        () => {
          const index = this.datasets.indexOf(item);
          this.datasets.splice(index, 1);
          this.log.success('Data set deleted'/*, item, 'Delete data set'*/);
        },
        (error) => this.log.error('The data set could not be deleted. Please try again.'/*, error, 'Delete data set'*/)
      );
  }

  close() {
    this.router.navigate(['overview']);
  }

}
