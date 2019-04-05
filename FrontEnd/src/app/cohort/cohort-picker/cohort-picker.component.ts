import {Component, Input, OnInit} from '@angular/core';
import {Cohort} from '../models/Cohort';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {CohortService} from '../cohort.service';
import {LoggerService} from 'eds-angular4';

@Component({
  selector: 'app-cohort-picker',
  templateUrl: './cohort-picker.component.html',
  styleUrls: ['./cohort-picker.component.css']
})
export class CohortPickerComponent implements OnInit {
  @Input() resultData: Cohort[];
  searchData: string;
  searchResults: Cohort[];

  public static open(modalService: NgbModal, cohorts: Cohort[]) {
    const modalRef = modalService.open(CohortPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], cohorts);

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private cohortService: CohortService) { }

  ngOnInit() {
  }
  search() {
    const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }
    vm.cohortService.search(vm.searchData)
      .subscribe(
        (result) => vm.searchResults = result,
        (error) => vm.log.error(error)
      );
  }

  private addToSelection(match: Cohort) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      this.resultData.push(match);
    }
  }

  private removeFromSelection(match: Cohort) {
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
