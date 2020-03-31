import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {LoggerService} from "dds-angular8";
import {Documentation} from "../models/Documentation";

@Component({
  selector: 'app-documentation',
  templateUrl: './documentation.component.html',
  styleUrls: ['./documentation.component.scss']
})
export class DocumentationComponent implements OnInit {

  file: File;
  pdfSrc: any;
  document: Documentation;

  constructor(public dialogRef: MatDialogRef<DocumentationComponent>,
              private log: LoggerService) {
  }

  ngOnInit() {
  }

  fileChange(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file = fileList[0];
    } else {
      this.file = null;
    }
  }

  ok() {
    const vm = this;
    const myReader: FileReader = new FileReader();
    myReader.onloadend = function(e) {
      vm.pdfSrc = myReader.result;
      const newDoc: Documentation = new Documentation();
      newDoc.fileData = myReader.result.toString();
      newDoc.title = vm.file.name;
      newDoc.filename = vm.file.name;
      vm.document = newDoc;
      vm.dialogRef.close(vm.document);
    }
    myReader.readAsDataURL(vm.file);
  }

  cancel() {
    this.dialogRef.close();
  }
}
