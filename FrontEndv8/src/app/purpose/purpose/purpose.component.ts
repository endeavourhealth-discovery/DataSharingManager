import {Component, Inject, OnInit} from '@angular/core';
import {Purpose} from "../../models/Purpose";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {LoggerService} from "dds-angular8";

export interface DialogData {
  resultData: Purpose[];
  type: string;
  index: number;
}

@Component({
  selector: 'app-purpose',
  templateUrl: './purpose.component.html',
  styleUrls: ['./purpose.component.scss']
})
export class PurposeComponent implements OnInit {

  data : Purpose[];
  type: string;
  index: number;

  title = '';
  detail = '';
  editMode = false;

  constructor(public dialogRef: MatDialogRef<PurposeComponent>,
              @Inject(MAT_DIALOG_DATA) public dataIn: DialogData,
              private log: LoggerService) {

    this.data = dataIn.resultData;
    this.type = dataIn.type;
    this.index = dataIn.index;
  }

  ngOnInit() {
    if (this.index > -1) {
      this.editMode = true;
      this.edit();
    }
  }

  edit() {
    this.title = this.data[this.index].title;
    this.detail = this.data[this.index].detail;
  }

  cancel() {
    this.dialogRef.close();
  }

  add() {
    let newPurpose: Purpose = new Purpose();
    newPurpose.title = this.title;
    newPurpose.detail = this.detail;
    this.data.push(newPurpose);
    this.dialogRef.close(this.data);
  }

  addAnother() {
    let newPurpose: Purpose = new Purpose();
    newPurpose.title = this.title;
    newPurpose.detail = this.detail;
    this.data.push(newPurpose);
    this.title = '';
    this.detail = '';
  }

  save() {
    this.data[this.index].title = this.title;
    this.data[this.index].detail = this.detail;
    this.dialogRef.close(this.data);
  }

  saveAndAddAnother() {
    this.data[this.index].title = this.title;
    this.data[this.index].detail = this.detail;
    this.editMode = false;
    this.title = '';
    this.detail = '';
  }
}
