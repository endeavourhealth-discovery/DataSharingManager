import { Component, OnInit } from '@angular/core';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {ActivatedRoute, Router} from "@angular/router";
import {LoggerService, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";
import {ValueSetsService} from "../value-sets.service";
//TODO remove temp code -start
import {SchedulerComponent} from "../../scheduler/scheduler/scheduler.component";
import {Schedule} from "../../scheduler/models/Schedule";
import {ValueSets} from "../../models/ValueSets";
//TODO remove temp code -end

@Component({
  selector: 'app-value-sets',
  templateUrl: './value-sets.component.html',
  styleUrls: ['./value-sets.component.scss']
})
export class ValueSetsComponent implements OnInit {

  public activeProject: UserProject;
  private paramSubscriber: any;

  //TODO remove temp code -start
  schedule: Schedule;
  //TODO remove temp code -end

  valueSets: ValueSets[];
  totalItems: number;
  detailsToShow = new ValueSets().getDisplayItems();
  loadingComplete = false;
  pageNumber = 1;
  pageSize = 20;
  searchData = '';
  orderColumn = 'name';
  descending = false;
  allowEdit = false;

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

  private search() {
    this.loadingComplete = false;
    console.log('searching', this.pageNumber);
    this.valueSetService.search(this.searchData, this.pageNumber, this.pageSize, this.orderColumn, this.descending)
      .subscribe(result => {
          this.valueSets = result;
          console.log(result);
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The value sets could not be loaded. Please try again.'/*, error, 'Load value sets'*/);
          this.loadingComplete = true;
        }
      );
  }

  getTotal() {
    this.valueSetService.getTotalCount(this.searchData)
      .subscribe(
        (result) => {
          this.totalItems = result;
        },
        (error) => console.log(error)
      );
  }

  delete(items: ValueSets[]) {
    this.valueSetService.delete(items).
    subscribe(
      (result) => {
        console.log("Delete successful.")
        this.search();
        this.getTotal();
      },
      (error) => console.log(error)
    );
  }

  itemClicked(item: ValueSets) {
    console.log(item);
  }

  pageChange($event) {
    this.pageNumber = $event.pageIndex + 1; // pagination index starts at 0, mySQL is 1
    this.pageSize = $event.pageSize;
    this.search();
  }

  onOrderChange($event) {
    this.orderColumn = $event.active;
    this.descending = $event.direction == 'desc' ? true : false;
    this.search();
  }

  onSearch($event) {

    this.searchData = $event;
    this.pageNumber = 1;
    this.valueSets = [];
    this.search();
    this.getTotalCount();
  }

  getTotalCount() {
    this.valueSetService.getTotalCount(this.searchData)
      .subscribe(
        (result) => {
          this.totalItems = result;
          console.log(result);
        },
        (error) => console.log(error)
      );
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
