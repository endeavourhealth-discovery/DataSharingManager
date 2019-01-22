import {Component, Input, OnInit} from '@angular/core';
import {Dpa} from "../models/Dpa";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LoggerService} from "eds-angular4";
import {DataProcessingAgreementService} from "../data-processing-agreement.service";

@Component({
  selector: 'app-data-processing-agreement-picker',
  templateUrl: './data-processing-agreement-picker.component.html',
  styleUrls: ['./data-processing-agreement-picker.component.css']
})
export class DataProcessingAgreementPickerComponent implements OnInit {
  @Input() resultData : Dpa[];
  searchData : string;
  searchResults : Dpa[];

  public static open(modalService: NgbModal, dpas: Dpa[]) {
    const modalRef = modalService.open(DataProcessingAgreementPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], dpas);

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private dpaService: DataProcessingAgreementService) { }

  ngOnInit() {
  }

  search() {
    const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }
    vm.dpaService.search(vm.searchData)
      .subscribe(
        (result) => vm.searchResults = result,
        (error) => vm.log.error(error)
      );
  }

  private addToSelection(match: Dpa) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      this.resultData.push(match);
    }
  }

  private removeFromSelection(match: Dpa) {
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
