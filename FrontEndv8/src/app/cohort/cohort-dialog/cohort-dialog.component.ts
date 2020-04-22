import {Component, Inject, OnInit} from '@angular/core';
import {Cohort} from "../models/Cohort";
import {LoggerService, UserManagerService} from "dds-angular8";
import {CohortService} from "../cohort.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {UserProject} from "dds-angular8/user-manager";

export interface DialogData {
  mode: string;
  uuid: string;
}

@Component({
  selector: 'app-cohort-dialog',
  templateUrl: './cohort-dialog.component.html',
  styleUrls: ['./cohort-dialog.component.scss']
})
export class CohortDialogComponent implements OnInit {

  cohort: Cohort = <Cohort>{};
  public activeProject: UserProject;
  mode: string;
  uuid: string;

  constructor(public dialogRef: MatDialogRef<CohortDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService,
              private cohortService: CohortService,
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
    this.cohort = {
      name : ''
    } as Cohort;
  }

  load(uuid: string) {
    this.cohortService.getCohort(uuid)
      .subscribe(result => {
          this.cohort = result;
        },
        error => this.log.error('The cohort could not be loaded. Please try again.')
      );
  }

  ok() {
    this.cohortService.saveCohort(this.cohort)
      .subscribe(saved => {
          this.cohort.uuid = saved;
          this.dialogRef.close(this.cohort);
        },
        error => this.log.error('The cohort could not be saved. Please try again.')
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
