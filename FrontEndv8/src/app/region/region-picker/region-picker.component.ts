import {Component, OnInit, Input, Inject, ViewChild} from '@angular/core';
import {RegionService} from '../region.service';
import {Region} from "../../models/Region";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {StandardPickerData} from "../../models/StandardPickerData";
import {LoggerService} from "dds-angular8";
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";

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

  /*public static open(modalService: NgbModal, regions: Region[], uuid: string = '', limit: number = 0) {
    const modalRef = modalService.open(RegionPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], regions);
    modalRef.componentInstance.uuid = uuid;
    modalRef.componentInstance.limit = limit;

    return modalRef;
  }*/

  constructor(
    public dialogRef: MatDialogRef<RegionPickerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: StandardPickerData,
    private regionService: RegionService,
    private log: LoggerService) {


  }

  ngOnInit() {
    this.search();
  }

  search() {
    /*const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }*/
    console.log('data', this.data);
    const regionUUID = this.data.uuid;
    this.regionService.getAllRegions(this.data.userId)
      .subscribe(
        (result) => this.searchResults = result.filter(function(x) {return x.uuid != regionUUID; }),
        (error) => this.log.error(error)
      );
  }
/*

  private addToSelection(match: Region) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      if (this.limit == 1) {
        this.resultData = [];
        this.resultData.push(match);
      } else {
        this.resultData.push(match);
      }
    }
  }

  private removeFromSelection(match: Region) {
    const index = this.resultData.indexOf(match, 0);
    if (index > -1) {
      this.resultData.splice(index, 1);
    }
  }
*/

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close([]);
  }

}
