import {Component, Inject, OnInit} from '@angular/core';
import {DataSet} from "../models/Dataset";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {LoggerService, UserManagerService} from "dds-angular8";
import {DataSetService} from "../data-set.service";

export interface DialogData {
  mode: string;
  uuid: string;
}

@Component({
  selector: 'app-data-set-dialog',
  templateUrl: './data-set-dialog.component.html',
  styleUrls: ['./data-set-dialog.component.scss']
})
export class DataSetDialogComponent implements OnInit {

  dataset: DataSet = <DataSet>{};
  public activeProject: UserProject;
  mode: string;
  uuid: string;

  constructor(public dialogRef: MatDialogRef<DataSetDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService,
              private datasetService: DataSetService,
              private userManagerNotificationService: UserManagerService,
              public dialog: MatDialog) {

    this.uuid = data.uuid;
    this.mode = data.mode;
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    this.performAction(this.mode, this.uuid);
  }

  protected performAction(action: string, itemUuid: string) {
    switch (action) {
      case 'add':
        this.create();
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  create() {
    this.dataset = {
      name : ''
    } as DataSet;
  }

  load(uuid: string) {
    this.datasetService.getDataSet(uuid)
      .subscribe(result => {
          this.dataset = result;
        },
        error => this.log.error('The data set could not be loaded. Please try again.')
      );
  }

  ok() {
    this.datasetService.saveDataSet(this.dataset)
      .subscribe(saved => {
          this.dataset.uuid = saved;
          this.dialogRef.close(this.dataset);
        },
        error => this.log.error('The data set could not be saved. Please try again.')
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
