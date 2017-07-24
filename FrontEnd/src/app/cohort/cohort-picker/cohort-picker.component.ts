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
    modalRef.componentInstance.resultData = jQuery.extend(true, [], cohorts);

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private cohortService: CohortService) { }

  ngOnInit() {
  }
  private search() {
    let vm = this;
    if (vm.searchData.length < 3)
      return;
    vm.cohortService.search(vm.searchData)
      .subscribe(
        (result) => vm.searchResults = result,
        (error) => vm.log.error(error)
      );
  }

  private addToSelection(match: Cohort) {
    if ($.grep(this.resultData, function(o: Cohort) { return o.uuid === match.uuid; }).length === 0)
      this.resultData.push(match);
  }

  private removeFromSelection(match: Cohort) {
    let index = this.resultData.indexOf(match, 0);
    if (index > -1)
      this.resultData.splice(index, 1);
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
