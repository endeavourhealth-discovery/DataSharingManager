import {Component, Inject, OnInit} from '@angular/core';
import {Address} from "../models/Address";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";

export interface DialogData {
  address: Address;
}

@Component({
  selector: 'app-address-dialog',
  templateUrl: './address-dialog.component.html',
  styleUrls: ['./address-dialog.component.scss']
})
export class AddressDialogComponent implements OnInit {

  address: Address = <Address>{};

  constructor(public dialogRef: MatDialogRef<AddressDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              public dialog: MatDialog) {
    this.address = data.address;
  }

  ngOnInit() {
  }

  ok() {
    this.dialogRef.close(this.address);
  }

  cancel() {
    this.dialogRef.close();
  }
}
