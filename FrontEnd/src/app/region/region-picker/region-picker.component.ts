import { Component, OnInit, Input } from '@angular/core';
import {Region} from '../models/Region';
import {RegionService} from '../region.service';
import {LoggerService} from 'eds-angular4';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-region-picker',
  templateUrl: './region-picker.component.html',
  styleUrls: ['./region-picker.component.css'],
  providers: [RegionService]
})
export class RegionPickerComponent implements OnInit {
  @Input() resultData: Region[];
  searchData: string;
  searchResults: Region[];
  uuid: string;
  limit: number;

  public static open(modalService: NgbModal, regions: Region[], uuid: string = '', limit: number = 0) {
    const modalRef = modalService.open(RegionPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], regions);
    modalRef.componentInstance.uuid = uuid;
    modalRef.componentInstance.limit = limit;

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private regionService: RegionService) { }

  ngOnInit() {
  }

  search() {
    const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }
    console.log(vm.uuid);
    vm.regionService.search(vm.searchData)
      .subscribe(
        (result) => vm.searchResults = result.filter(function(x) {return x.uuid != vm.uuid; }),
        (error) => vm.log.error(error)
      );
  }

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

  ok() {
    this.activeModal.close(this.resultData);
    console.log('OK Pressed');
  }

  cancel() {
    this.activeModal.dismiss('cancel');
    console.log('Cancel Pressed');
  }

}
