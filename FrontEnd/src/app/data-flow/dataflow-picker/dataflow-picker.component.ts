import {Component, Input, OnInit} from '@angular/core';
import {DataFlow} from '../models/DataFlow';
import {DataFlowService} from '../data-flow.service';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {LoggerService} from 'eds-angular4';

@Component({
  selector: 'app-dataflow-picker',
  templateUrl: './dataflow-picker.component.html',
  styleUrls: ['./dataflow-picker.component.css']
})
export class DataflowPickerComponent implements OnInit {
  @Input() resultData: DataFlow[];
  searchData: string;
  searchResults: DataFlow[];

  public static open(modalService: NgbModal, dataFlows: DataFlow[]) {
    const modalRef = modalService.open(DataflowPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], dataFlows);

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private dataFlowService: DataFlowService) { }

  ngOnInit() {
  }

  search() {
    const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }
    vm.dataFlowService.search(vm.searchData)
      .subscribe(
        (result) => vm.searchResults = result,
        (error) => vm.log.error(error)
      );
  }

  private addToSelection(match: DataFlow) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      this.resultData.push(match);
    }
  }

  private removeFromSelection(match: DataFlow) {
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
