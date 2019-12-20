import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Organisation} from "../../models/Organisation";
import {OrganisationService} from "../organisation.service";
import {LoggerService} from "dds-angular8";
import {OrganisationPickerData} from "../../models/OrganisationPickerData";
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";

@Component({
  selector: 'app-organisation-picker',
  templateUrl: './organisation-picker.component.html',
  styleUrls: ['./organisation-picker.component.scss']
})
export class OrganisationPickerComponent implements OnInit {
  resultData: Organisation[];
  searchData: string;
  availableOrgs: Organisation[];
  searchResults: Organisation[];
  multipleSearchResults: Organisation[];
  multipleSearchMissing: string[];
  multipleAddedCount = 0;
  showMultipleMessage = false;
  odsCodes: string;
  orgDetailsToShow = new Organisation().getDisplayItems();

  @ViewChild('orgPicker', { static: false }) orgPicker: GenericTableComponent;

  constructor(
    public dialogRef: MatDialogRef<OrganisationPickerComponent>,
              @Inject(MAT_DIALOG_DATA) public data: OrganisationPickerData,
              private organisationService: OrganisationService,
              private log: LoggerService) {


  }

  ngOnInit() {
    /*if (this.regionUUID != '') {
      this.getRegionOrganisations(this.regionUUID);
    } else if (this.dsaUUID != '') {
      if (this.searchType == 'publisher') {
        this.getDSAPublishers(this.dsaUUID);
      } else if (this.searchType == 'subscriber') {
        this.getDSASubscribers(this.dsaUUID);
      }
    }*/
  }

  search($event: KeyboardEvent) {
    console.log('gete');
    if (this.searchData.length < 3) {
      return;
    }

    if (this.data.regionUUID != '') {
      this.organisationService.searchOrganisationsInParentRegion(this.data.regionUUID, this.searchData)
        .subscribe(
          (result) => this.searchResults = result.filter(function (x) {
            return x.uuid != this.uuid;
          }),
          (error) => this.log.error(error)
        );
    } else if (this.data.dsaUUID != '' && this.data.searchType == 'publisher') {
      this.organisationService.searchPublishersInDSA(this.data.dsaUUID, this.searchData)
        .subscribe(
          (result) => this.searchResults = result.filter(function (x) {
            return x.uuid != this.uuid;
          }),
          (error) => this.log.error(error)
        );
    } else if (this.data.dsaUUID != '' && this.data.searchType == 'subscriber') {
      this.organisationService.searchSubscribersInDSA(this.data.dsaUUID, this.searchData)
        .subscribe(
          (result) => this.searchResults = result.filter(function (x) {
            return x.uuid != this.uuid;
          }),
          (error) => this.log.error(error)
        );
    } else {
      console.log('searching');
      const orgUUID = this.data.uuid;
      this.organisationService.search(this.searchData, this.data.searchType)
        .subscribe(
          (result) => {console.log(result); this.searchResults = result.filter(function (x) {
            return x.uuid != orgUUID;
          })},
          (error) => this.log.error(error)
        );
    }
  }

  searchMultiple() {
    this.showMultipleMessage = false;
    var odsList = this.odsCodes.replace(/\n/g, ',').split(',');

    if (this.data.regionUUID != '') {
      this.organisationService.searchOrganisationsInParentRegionWithOdsList(this.data.regionUUID, odsList)
        .subscribe(
          (result) => {
            this.multipleSearchResults = result,
              this.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => this.log.error(error)
        );
    } else if (this.data.dsaUUID != '' && this.data.searchType == 'publisher') {
      this.organisationService.searchPublishersFromDSAWithOdsList(this.data.dsaUUID, odsList)
        .subscribe(
          (result) => {
            this.multipleSearchResults = result,
              this.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => this.log.error(error)
        );
    } else if (this.data.dsaUUID != '' && this.data.searchType == 'subscriber') {
      this.organisationService.searchSubscribersFromDSAWithOdsList(this.data.dsaUUID, odsList)
        .subscribe(
          (result) => {
            this.multipleSearchResults = result,
              this.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => this.log.error(error)
        );
    } else {
      this.organisationService.getMultipleOrganisationsFromODSList(odsList)
        .subscribe(
          (result) => {
            this.multipleSearchResults = result,
              this.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => this.log.error(error)
        );
    }
  }

  private addToSelection(match: Organisation) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      this.resultData.push(match);
    }
  }

  private removeFromSelection(match: Organisation) {
    const index = this.resultData.indexOf(match, 0);
    if (index > -1) {
      this.resultData.splice(index, 1);
    }
  }

  addMultiple() {
    this.showMultipleMessage = false;
    this.multipleAddedCount = 0;
    for (let match of this.multipleSearchResults) {
      if (!this.resultData.some(x => x.uuid === match.uuid)) {
        this.resultData.push(match);
        this.multipleAddedCount++;
      }
    }
    this.showMultipleMessage = true;
  }

  ok() {
    this.moveSelectedOrgsIntoResultData();
    console.log();
    this.dialogRef.close(this.orgPicker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }

  checkAllOrganisations(ev) {
    console.log('selecting all');
    this.availableOrgs.forEach(x => x.selected = ev.target.checked);
  }

  /*getRegionOrganisations(regionUUID: string) {
    this.regionService.getRegionOrganisations(regionUUID)
      .subscribe(
        result => {
          this.availableOrgs = result;
          this.availableOrgs.forEach(x => x.selected = false);
        },
        error => this.log.error('The region organisations could not be loaded. Please try again.')
      );
  }

  getDSAPublishers(dsaUUID: string) {
    this.dsaService.getPublishers(dsaUUID)
      .subscribe(
        result => {
          this.availableOrgs = result;
          this.availableOrgs.forEach(x => x.selected = false);
        },
        error => this.log.error('The dsa publishers could not be loaded. Please try again.')
      );
  }

  getDSASubscribers(dsaUUID: string) {
    this.dsaService.getSubscribers(dsaUUID)
      .subscribe(
        result => {
          this.availableOrgs = result;
          this.availableOrgs.forEach(x => x.selected = false);
        },
        error => this.log.error('The dsa subscribers could not be loaded. Please try again.')
      );
  }*/

  onTabChange(ev) {
    console.log(this.availableOrgs);
    if (ev.nextId == 'select') {
      for (let match of this.resultData) {
        let foundOrg = this.availableOrgs.findIndex(o => o.uuid === match.uuid);
        if (foundOrg > -1) {
          this.availableOrgs[foundOrg].selected = true;
        }
      }
    } else {
      this.moveSelectedOrgsIntoResultData();
    }
  }

  moveSelectedOrgsIntoResultData() {
    if (this.availableOrgs) {
      for (let match of this.availableOrgs) {
        if (match.selected) {
          if (!this.resultData.some(x => x.uuid === match.uuid)) {
            this.resultData.push(match);
          }
        }
      }
    }
  }

  clear() {
    this.searchData = '';
  }

}
