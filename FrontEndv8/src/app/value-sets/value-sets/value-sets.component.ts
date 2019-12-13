import { Component, OnInit } from '@angular/core';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {ActivatedRoute, Router} from "@angular/router";
import {LoggerService, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";
import {ValueSetsService} from "../value-sets.service";
//TODO remove temp code -start
import {SchedulerComponent} from "../../scheduler/scheduler/scheduler.component";
import {Schedule} from "../../scheduler/models/Schedule";
//TODO remove temp code -end

@Component({
  selector: 'app-value-sets',
  templateUrl: './value-sets.component.html',
  styleUrls: ['./value-sets.component.scss']
})
export class ValueSetsComponent implements OnInit {

  private paramSubscriber: any;
  allowEdit = false;
  public activeProject: UserProject;

  //TODO remove temp code -start
  schedule: Schedule;
  //TODO remove temp code -end

  constructor(private valueSetService: ValueSetsService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService,
              public dialog: MatDialog) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
    }

    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.search();
        this.getTotal();
      });
  }

  search() {

  }

  getTotal() {

  }

  add() {

  }

  close() {

  }

  //TODO remove temp code -start
  setSchedule() {
    const dialogRef = this.dialog.open(SchedulerComponent, {
      height: '610px',
      width: '1200px',
      data: {schedule: this.schedule, allowTime: true},
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.schedule = result;
      }
    });
  }
  //TODO remove temp code -end
}
