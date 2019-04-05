import {Component, Input, OnInit} from '@angular/core';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
import {LoggerService} from 'eds-angular4';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Dsa} from '../models/Dsa';

@Component({
  selector: 'app-data-sharing-agreement-picker',
  templateUrl: './data-sharing-agreement-picker.component.html',
  styleUrls: ['./data-sharing-agreement-picker.component.css']
})
export class DataSharingAgreementPickerComponent implements OnInit {
  @Input() resultData: Dsa[];
  searchData: string;
  searchResults: Dsa[];
  limit: number;

  public static open(modalService: NgbModal, dsas: Dsa[], limit: number = 0) {
    const modalRef = modalService.open(DataSharingAgreementPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], dsas);
    modalRef.componentInstance.limit = limit;

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private dsaService: DataSharingAgreementService) { }

  ngOnInit() {
  }

  search() {
    const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }
    vm.dsaService.search(vm.searchData)
      .subscribe(
        (result) => vm.searchResults = result,
        (error) => vm.log.error(error)
      );
  }

  private addToSelection(match: Dsa) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      if (this.limit == 1){
        this.resultData = [];
        this.resultData.push(match);
      } else {
        this.resultData.push(match);
      }
    }
  }

  private removeFromSelection(match: Dsa) {
    const index = this.resultData.indexOf(match, 0);
    if (index > -1) {
      this.resultData.splice(index, 1);
    }
  }

  ok() {
    this.activeModal.close(this.resultData);
  }

  cancel() {
    this.activeModal.dismiss('cancel');
  }

}
