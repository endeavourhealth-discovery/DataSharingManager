import {Component, OnInit, ViewChild} from '@angular/core';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {ActivatedRoute, Router} from "@angular/router";
import {GenericTableSspComponent, LoggerService, UserManagerService} from "dds-angular8";
import {MatDialogRef} from "@angular/material/dialog";
import {ValueSetsService} from "../value-sets.service";
import {ValueSets} from "../models/ValueSets";

@Component({
  selector: 'app-value-sets',
  templateUrl: './value-sets.component.html',
  styleUrls: ['./value-sets.component.scss']
})
export class ValueSetsComponent implements OnInit {

  @ViewChild('valueSetsTable', { static: false }) valueSetsTable: GenericTableSspComponent;

  public activeProject: UserProject;
  private paramSubscriber: any;

  valueSets: ValueSets[];
  selectedValueSets: ValueSets[];
  totalItems: number;
  detailsToShow = new ValueSets().getDisplayItems();
  loadingComplete = false;
  pageNumber = 1;
  pageSize = 10;
  searchData = '';
  orderColumn = 'name';
  descending = false;
  allowEdit = true;

  constructor(
    public dialogRef: MatDialogRef<ValueSetsComponent>,
    private valueSetService: ValueSetsService,
    private router: Router,
    private route: ActivatedRoute,
    private userManagerNotificationService: UserManagerService,
    private log: LoggerService) {

  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.search();
        this.getTotal();
      });
  }

  private search() {
    this.loadingComplete = false;
    this.valueSetService.search(this.searchData, this.pageNumber, this.pageSize, this.orderColumn, this.descending)
      .subscribe(result => {
          this.valueSets = result;
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The value sets could not be loaded. Please try again.');
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
    console.log(items);
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

  ok() {
    this.selectedValueSets = this.valueSetsTable.selection.selected;
    this.dialogRef.close(this.selectedValueSets);
  }

  cancel() {
    this.dialogRef.close();
  }
}
