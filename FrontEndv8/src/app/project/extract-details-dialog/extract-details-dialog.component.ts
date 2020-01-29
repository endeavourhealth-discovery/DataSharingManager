import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {ExtractTechnicalDetails} from "../models/ExtractTechnicalDetails";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {LoggerService} from "dds-angular8";

export interface DialogData {
  extractTechnicalDetail: ExtractTechnicalDetails;
}

@Component({
  selector: 'app-extract-details-dialog',
  templateUrl: './extract-details-dialog.component.html',
  styleUrls: ['./extract-details-dialog.component.scss']
})
export class ExtractDetailsDialogComponent implements OnInit {

  extractTechnicalDetail: ExtractTechnicalDetails = <ExtractTechnicalDetails>{};
  file1: File;
  file2: File;
  file3: File;
  file4: File;
  src: any;
  @ViewChild("fileUploader", { static: false }) fileUploader: ElementRef;

  constructor(public dialogRef: MatDialogRef<ExtractDetailsDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService) {

    if (data.extractTechnicalDetail) {
      this.extractTechnicalDetail = data.extractTechnicalDetail;
    } else {
      this.extractTechnicalDetail = new ExtractTechnicalDetails();
    }
  }

  ngOnInit() {
  }

  file1Change(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file1 = fileList[0];
    } else {
      this.file1 = null;
    }
    const myReader: FileReader = new FileReader();
    const vm = this;
    myReader.onloadend = function(e) {
      vm.log.success('Uploading SFTP host public key file complete.');
      vm.extractTechnicalDetail.sftpHostPublicKeyFilename = vm.file1.name;
      vm.extractTechnicalDetail.sftpHostPublicKeyFileData = myReader.result.toString();
    };
    myReader.readAsDataURL(this.file1);
  }

  file2Change(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file2 = fileList[0];
    } else {
      this.file2 = null;
    }
    const myReader: FileReader = new FileReader();
    const vm = this;
    myReader.onloadend = function(e) {
      vm.log.success('Uploading SFTP client private key file complete.');
      vm.extractTechnicalDetail.sftpClientPrivateKeyFilename = vm.file2.name;
      vm.extractTechnicalDetail.sftpClientPrivateKeyFileData = myReader.result.toString();
    };
    myReader.readAsDataURL(this.file2);
  }

  file3Change(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file3 = fileList[0];
    } else {
      this.file3 = null;
    }
    const myReader: FileReader = new FileReader();
    const vm = this;
    myReader.onloadend = function(e) {
      vm.log.success('Uploading customer public key file complete.');
      vm.extractTechnicalDetail.pgpCustomerPublicKeyFilename = vm.file3.name;
      vm.extractTechnicalDetail.pgpCustomerPublicKeyFileData = myReader.result.toString();
    };
    myReader.readAsDataURL(this.file3);
  }

  file4Change(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file4 = fileList[0];
    } else {
      this.file4 = null;
    }
    const myReader: FileReader = new FileReader();
    const vm = this;
    myReader.onloadend = function(e) {
      vm.log.success('Uploading internal public key file complete.');
      vm.extractTechnicalDetail.pgpInternalPublicKeyFilename = vm.file4.name;
      vm.extractTechnicalDetail.pgpInternalPublicKeyFileData = myReader.result.toString();
    };
    myReader.readAsDataURL(this.file4);
  }

  clearExtraTechDetails(whichFile: number) {
    if (whichFile == 1) {
      this.extractTechnicalDetail.sftpHostPublicKeyFilename = null;
      this.extractTechnicalDetail.sftpHostPublicKeyFileData = null;
    } else if (whichFile == 2) {
      this.extractTechnicalDetail.sftpClientPrivateKeyFilename = null;
      this.extractTechnicalDetail.sftpClientPrivateKeyFileData = null;
    } else if (whichFile == 3) {
      this.extractTechnicalDetail.pgpCustomerPublicKeyFilename = null;
      this.extractTechnicalDetail.pgpCustomerPublicKeyFileData = null;
    } else if (whichFile == 4) {
      this.extractTechnicalDetail.pgpInternalPublicKeyFilename = null;
      this.extractTechnicalDetail.pgpInternalPublicKeyFileData = null;
    }
  }

  ok() {
    this.dialogRef.close(this.extractTechnicalDetail);
  }

  cancel() {
    this.dialogRef.close();
  }
}
