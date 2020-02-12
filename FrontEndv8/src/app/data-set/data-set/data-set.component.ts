import {Component, OnInit, ViewChild} from '@angular/core';
import {DataSet} from '../models/Dataset';
import {DataSetService} from '../data-set.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";
import {DataSetDialogComponent} from "../data-set-dialog/data-set-dialog.component";

@Component({
  selector: 'app-data-set',
  templateUrl: './data-set.component.html',
  styleUrls: ['./data-set.component.css']
})
export class DataSetComponent implements OnInit {

  @ViewChild('datasetsTable', { static: false }) datasetsTable: GenericTableComponent;

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
              private log: LoggerService,
              public dialog: MatDialog) {
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
    const dialogRef = this.dialog.open(DataSetDialogComponent, {
      data: {mode: 'add', uuid: ''},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/dataSet', result.uuid, 'edit']);
      }
    });
  }

  edit(item: DataSet) {
    this.router.navigate(['/dataSet', item.uuid, 'edit']);
  }

  itemClicked(dataset: DataSet) {
    this.router.navigate(['/dataSet', dataset.uuid, 'edit']);
  }

  delete() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete data set', 'Are you sure you want to delete data set(s)?',
      'Delete data set', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            let ids = [];
            for (var i = 0; i < this.datasetsTable.selection.selected.length; i++) {
              let dataset = this.datasetsTable.selection.selected[i];
              this.datasets.forEach( (item, index) => {
                if(item === dataset) {
                  this.datasets.splice(index,1);
                  this.datasetsTable.updateRows();
                  ids.push(item.uuid);
                }
              });
            }
            this.dataSetService.deleteDataSet(ids).subscribe(
              () => {
                this.log.success('Delete successful.');
              }
            );
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  close() {
    this.router.navigate(['overview']);
  }

}
