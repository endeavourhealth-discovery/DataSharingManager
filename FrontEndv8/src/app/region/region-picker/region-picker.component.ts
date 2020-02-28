import {Component, OnInit, Input, Inject, ViewChild} from '@angular/core';
import {RegionService} from '../region.service';
import {Region} from "../models/Region";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {StandardPickerData} from "../../models/StandardPickerData";
import {GenericTableComponent, LoggerService} from "dds-angular8";
import {Organisation} from "../../organisation/models/Organisation";

@Component({
  selector: 'app-region-picker',
  templateUrl: './region-picker.component.html',
  styleUrls: ['./region-picker.component.css'],
  providers: [RegionService]
})
export class RegionPickerComponent implements OnInit {
  resultData: Region[];
  searchData: string;
  searchResults: Region[];
  regionDetailsToShow = new Region().getDisplayItems();
  type: string;

  @ViewChild('picker', { static: false }) picker: GenericTableComponent;

  constructor(
    public dialogRef: MatDialogRef<RegionPickerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: StandardPickerData,
    private regionService: RegionService,
    private log: LoggerService) {

    this.type = data.type;
    if (!this.type) {
      this.type = '';
    }

  }

  ngOnInit() {
    this.search();
  }

  clear() {
    this.searchData = '';
  }

  search() {
    let filterResults: Region[];
    const regionUUID = this.data.uuid;
    const existing = this.data.existing;
    this.regionService.getAllRegions(this.data.userId)
      .subscribe(
        (result) => {
          filterResults = result.filter((x) => !existing.filter(y => y.uuid === x.uuid).length);
          this.searchResults = filterResults.filter(function(x) { return x.uuid != regionUUID; })
        },
        (error) => this.log.error(error)
      );
  }

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }

}
