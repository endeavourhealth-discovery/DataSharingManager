import {Component, OnInit, ViewChild} from '@angular/core';
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";
import {DataSet} from "../models/Dataset";
import {LoggerService} from "dds-angular8";
import {MatDialogRef} from "@angular/material/dialog";
import {DataSetService} from "../data-set.service";

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
          this.searchResults = result;
        }
      );
  }

  searchAll() {
    this.dataSetService.getAllDataSets()
      .subscribe(
        result => {
          this.searchResults = result;
        }
      );
  }

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }
}
