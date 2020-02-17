import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {Cohort} from '../models/Cohort';
import {CohortService} from '../cohort.service';
import {GenericTableComponent, LoggerService} from "dds-angular8";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {StandardPickerData} from "../../models/StandardPickerData";

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
              @Inject(MAT_DIALOG_DATA) public data: StandardPickerData,
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
          this.searchResults = this.filterResults(result);
        }
      );
  }

  filterResults(results: Cohort[]) {
    let filterResults: Cohort[];
    const existing = this.data.existing;

    filterResults = results.filter((x) => !existing.filter(y => y.uuid === x.uuid).length);

    return filterResults;
  }

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }

}
