import { Component, OnInit, Input } from '@angular/core';
import {Organisation} from '../models/Organisation';
import {OrganisationService} from '../organisation.service';
import {LoggerService} from 'eds-angular4';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {RegionService} from "../../region/region.service";

@Component({
  selector: 'app-organisation-picker',
  templateUrl: './organisation-picker.component.html',
  styleUrls: ['./organisation-picker.component.css']
})
export class OrganisationPickerComponent implements OnInit {
  @Input() resultData: Organisation[];
  searchData: string;
  searchResults: Organisation[];
  multipleSearchResults: Organisation[];
  multipleSearchMissing: string[];
  multipleAddedCount = 0;
  showMultipleMessage = false;
  searchType: string;
  uuid: string;
  regionUUID: string;
  dsaUUID: string;
  odsCodes: string;

  public static open(modalService: NgbModal, organisations: Organisation[], searchType: string, uuid: string = '',
                     regionUUID: string = '', dsaUUID : string = '') {
    const modalRef = modalService.open(OrganisationPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], organisations);
    modalRef.componentInstance.searchType = searchType;
    modalRef.componentInstance.regionUUID = regionUUID;
    modalRef.componentInstance.dsaUUID = dsaUUID;

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private organisationService: OrganisationService) { }

  ngOnInit() {
  }

  search() {
    const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }
    vm.organisationService.search(vm.searchData, vm.searchType)
      .subscribe(
        (result) => vm.searchResults = result.filter(function(x) {return x.uuid != vm.uuid; }),
        (error) => vm.log.error(error)
      );
  }

  searchMultiple() {
    const vm = this;
    vm.showMultipleMessage = false;
    var odsList = vm.odsCodes.replace(/\n/g, ',').split(',');

    vm.organisationService.getMultipleOrganisationsFromODSList(odsList)
      .subscribe(
        (result) => {
          vm.multipleSearchResults = result,
          vm.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
        },
        (error) => vm.log.error(error)
      );
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
    const vm = this;
    vm.showMultipleMessage = false;
    vm.multipleAddedCount = 0;
    for (let match of this.multipleSearchResults) {
      if (!this.resultData.some(x => x.uuid === match.uuid)) {
        this.resultData.push(match);
        vm.multipleAddedCount++;
      }
    }
    vm.showMultipleMessage = true;
  }

  ok() {
    this.activeModal.close(this.resultData);
    console.log('OK Pressed');
  }

  cancel() {
    this.activeModal.dismiss('cancel');
    console.log('Cancel Pressed');
  }

}
