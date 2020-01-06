import {Component, OnInit, ViewChild} from '@angular/core';
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";
import {Cohort} from '../models/Cohort';
import {CohortService} from '../cohort.service';
import {LoggerService} from "dds-angular8";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-cohort-picker',
  templateUrl: './cohort-picker.component.html',
  styleUrls: ['./cohort-picker.component.css']
})
export class CohortPickerComponent implements OnInit {

  searchData: string;
  searchResults: Cohort[];
  cohortDetailsToShow = new Cohort().getDisplayItems();
  allowEdit = true;

  @ViewChild('picker', { static: false }) picker: GenericTableComponent;

  constructor(public dialogRef: MatDialogRef<CohortPickerComponent>,
              private log: LoggerService,
              private cohortService: CohortService) {
  }

  ngOnInit() {
    this.searchAll()
  }

  clear() {
    this.searchData = '';
    this.searchAll();
  }

  search($event: KeyboardEvent) {
    if (this.searchData.length < 3) {
      return;
    }
    this.cohortService.search(this.searchData)
      .subscribe(
        result => this.searchResults = result,
        (error) => this.log.error(error)
      );
  }

  searchAll() {
    this.cohortService.getAllCohorts()
      .subscribe(
        result => {
          this.searchResults = result;
        }
      );
  }

  /*private addToSelection(match: Cohort) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      this.resultData.push(match);
    }
  }

  private removeFromSelection(match: Cohort) {
    const index = this.resultData.indexOf(match, 0);
    if (index > -1) {
      this.resultData.splice(index, 1);
    }
  }*/

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }

}
