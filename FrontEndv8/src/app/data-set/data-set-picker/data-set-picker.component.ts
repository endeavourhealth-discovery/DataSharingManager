import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {DataSet} from "../models/Dataset";
import {GenericTableComponent, LoggerService} from "dds-angular8";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DataSetService} from "../data-set.service";
import {StandardPickerData} from "../../models/StandardPickerData";

@Component({
  selector: 'app-data-set-picker',
  templateUrl: './data-set-picker.component.html',
  styleUrls: ['./data-set-picker.component.scss']
})
export class DataSetPickerComponent implements OnInit {

  searchData: string;
  searchResults: DataSet[];
  dataSetDetailsToShow = new DataSet().getDisplayItems();
  allowEdit = true;

  @ViewChild('picker', { static: false }) picker: GenericTableComponent;

  constructor(
    public dialogRef: MatDialogRef<DataSetPickerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: StandardPickerData,
    private dataSetService: DataSetService,
    private log: LoggerService) {
  }

  ngOnInit() {
    this.searchAll();
  }

  clear() {
    this.searchData = '';
    this.searchAll();
  }

  search($event: KeyboardEvent) {
    if (this.searchData.length < 3) {
      return;
    }

    this.dataSetService.search(this.searchData)
      .subscribe(
        result => {
          this.searchResults = this.filterResults(result);
        }
      );
  }

  filterResults(results: DataSet[]) {
    let filterResults: DataSet[];
    const existing = this.data.existing;

    filterResults = results.filter((x) => !existing.filter(y => y.uuid === x.uuid).length);

    return filterResults;
  }

  searchAll() {
    console.log(this.data.userId);
    if (this.data.userId == null) {
      this.dataSetService.getAllDataSets()
        .subscribe(
          result => {
            this.searchResults = this.filterResults(result);
          }
        );
    } else {
      this.dataSetService.getDataSetsBasedOnRegion(this.data.userId)
        .subscribe(
          result => {
            this.searchResults = this.filterResults(result);
          }
        );
    }
  }

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }
}
