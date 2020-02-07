import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA, MatDialog} from '@angular/material/dialog';
import {Organisation} from "../models/Organisation";
import {OrganisationService} from "../organisation.service";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent} from "dds-angular8";
import {OrganisationPickerData} from "../models/OrganisationPickerData";
import {RegionService} from "../../region/region.service";
import {DataSharingAgreementService} from "../../data-sharing-agreement/data-sharing-agreement.service";

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
  odsCodes: string = '';
  listSearch = true;
  orgDetailsToShow = new Organisation().getDisplayItems();

  @ViewChild('orgPicker', { static: false }) orgPicker: GenericTableComponent;
  @ViewChild('listPicker', { static: false }) listPicker: GenericTableComponent;

  constructor(
    public dialogRef: MatDialogRef<OrganisationPickerComponent>,
              @Inject(MAT_DIALOG_DATA) public data: OrganisationPickerData,
              private organisationService: OrganisationService,
              private regionService: RegionService,
              private dsaService: DataSharingAgreementService,
              private log: LoggerService,
              public dialog: MatDialog) {


  }

  ngOnInit() {
    if (!this.data.regionUUID && !this.data.dsaUUID) {
      this.listSearch = false;
    }
    if (this.data.regionUUID != '') {
      this.getRegionOrganisations(this.data.regionUUID);
    } else if (this.data.dsaUUID != '') {
      if (this.data.searchType == 'publisher') {
        this.getDSAPublishers(this.data.dsaUUID);
      } else if (this.data.searchType == 'subscriber') {
        this.getDSASubscribers(this.data.dsaUUID);
      }
    }
  }

  search() {
    if (this.searchData.length < 3) {
      return;
    }
    const orgUUID = this.data.uuid;
    this.organisationService.search(this.searchData, this.data.searchType, 1, 100)
      .subscribe(
        (result) => {
          this.searchResults = this.filterResults(result)
          console.log(this.searchResults);
        },
        (error) => this.log.error(error)
      );
  }

  filterResults(results: Organisation[]) {
    let filterResults: Organisation[];
    const existing = this.data.existingOrgs;

    filterResults = results.filter((x) => !existing.filter(y => y.uuid === x.uuid).length);

    if (this.data.uuid) {
      filterResults = filterResults.filter(function (x) {
        return x.uuid != this.data.uuid;
      })
    }

    return filterResults;
  }

  searchMultiple() {
    this.showMultipleMessage = false;
    var odsList = this.odsCodes.replace(/\n/g, ',').replace(/\r/g, '').replace(' ', '').split(',');
    odsList = odsList.filter(function(el) { return el; });

    this.organisationService.getMultipleOrganisationsFromODSList(odsList)
      .subscribe(
        (result) => {
          this.multipleSearchResults = result,
            this.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
        },
        (error) => this.log.error(error)
      );

  }

  copyToClipboard() {
    let listener = (e: ClipboardEvent) => {
      e.clipboardData.setData('text/plain', (this.multipleSearchMissing.join()));
      e.preventDefault();
    };

    document.addEventListener('copy', listener);
    document.execCommand('copy');
    document.removeEventListener('copy', listener);
  }

  replaceLineBreaks(event: ClipboardEvent) {

    let clipboardData = event.clipboardData;
    let pastedText = clipboardData.getData('text');
    this.odsCodes = this.odsCodes + pastedText.split(/\n/).join(',');
    event.preventDefault();

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
    if (this.listSearch) {
      this.dialogRef.close(this.listPicker.selection.selected);
    } else {
      this.dialogRef.close(this.orgPicker.selection.selected);
    }
  }

  cancel() {
    this.dialogRef.close();
  }

  getRegionOrganisations(regionUUID: string) {
    this.regionService.getRegionOrganisations(regionUUID)
      .subscribe(
        result => {
          this.availableOrgs = this.filterResults(result);
        },
        error => this.log.error('The region organisations could not be loaded. Please try again.')
      );
  }

  getDSAPublishers(dsaUUID: string) {
    this.dsaService.getPublishers(dsaUUID)
      .subscribe(
        result => {
          this.availableOrgs = this.filterResults(result);

        },
        error => this.log.error('The dsa publishers could not be loaded. Please try again.')
      );
  }

  getDSASubscribers(dsaUUID: string) {
    this.dsaService.getSubscribers(dsaUUID)
      .subscribe(
        result => {
          this.availableOrgs = this.filterResults(result);
        },
        error => this.log.error('The dsa subscribers could not be loaded. Please try again.')
      );
  }

  onTabChange(ev) {
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
