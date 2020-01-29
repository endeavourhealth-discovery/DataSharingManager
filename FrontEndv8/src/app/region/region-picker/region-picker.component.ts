import {Component, OnInit, Input, Inject, ViewChild} from '@angular/core';
import {RegionService} from '../region.service';
import {Region} from "../models/Region";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {StandardPickerData} from "../../models/StandardPickerData";
import {GenericTableComponent, LoggerService} from "dds-angular8";

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

  @ViewChild('picker', { static: false }) picker: GenericTableComponent;

  constructor(
    public dialogRef: MatDialogRef<RegionPickerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: StandardPickerData,
    private regionService: RegionService,
    private log: LoggerService) {
  }

  ngOnInit() {
    this.search();
  }

  clear() {
    this.searchData = '';
  }

  search() {
    console.log('data', this.data);
    const regionUUID = this.data.uuid;
    this.regionService.getAllRegions(this.data.userId)
      .subscribe(
        (result) => this.searchResults = result.filter(function(x) {return x.uuid != regionUUID; }),
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
