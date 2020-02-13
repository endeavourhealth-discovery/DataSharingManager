import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {GoogleMapsData} from "../models/GoogleMapsData";
import {Marker} from "../../region/models/Marker";

@Component({
  selector: 'app-google-maps-dialog',
  templateUrl: './google-maps-dialog.component.html',
  styleUrls: ['./google-maps-dialog.component.scss']
})
export class GoogleMapsDialogComponent implements OnInit {

  latitude: number = 53.8347266;
  longitude: number = -4.7194005;
  zoom: number = 5.5;
  markers: Marker[] = [];
  title: string;

  constructor(public dialogRef: MatDialogRef<GoogleMapsDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: GoogleMapsData,) {
    this.markers = this.data.markers;
    this.title = this.data.title;
  }

  ngOnInit() {
  }

  ok() {
    this.dialogRef.close();
  }

}
