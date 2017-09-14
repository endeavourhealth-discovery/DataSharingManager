import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataSharingSummaryService} from '../data-sharing-summary.service';
import {LoggerService, MessageBoxDialog} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataSharingSummary} from '../models/DataSharingSummary';
import {ToastsManager} from 'ng2-toastr';

@Component({
  selector: 'app-data-sharing-summary',
  templateUrl: './data-sharing-summary.component.html',
  styleUrls: ['./data-sharing-summary.component.css']
})
export class DataSharingSummaryComponent implements OnInit {
  dataSharingSummaries: DataSharingSummary[] = [];
  allowDelete = true;

  dssDetailsToShow = new DataSharingSummary().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dataSharingSummaryService: DataSharingSummaryService,
              private log: LoggerService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.getDataSharingSummaries();
  }

  getDataSharingSummaries() {
    const vm = this;
    vm.dataSharingSummaryService.getAllDataSharingSummaries()
      .subscribe(
        result => vm.dataSharingSummaries = result,
        error => vm.log.error('Failed to load data sharing summaries', error, 'Load data sharing summaries')
      );
  }

  add() {
    this.router.navigate(['/dataSharingSummary', 1, 'add']);
  }

  edit(item: DataSharingSummary) {
    this.router.navigate(['/dataSharingSummary', item.uuid, 'edit']);
  }

  delete(item: DataSharingSummary) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete Data Sharing Summary', 'Are you sure you want to delete the Data Sharing Summary?', 'Yes', 'No')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: DataSharingSummary) {
    const vm = this;
    vm.dataSharingSummaryService.deleteDataSharingSummary(item.uuid)
      .subscribe(
        () => {
          const index = vm.dataSharingSummaries.indexOf(item);
          vm.dataSharingSummaries.splice(index, 1);
          vm.log.success('Data Sharing Summary deleted', item, 'Delete Data Sharing Summary');
        },
        (error) => vm.log.error('Failed to delete Data Sharing Summary', error, 'Delete Data Sharing Summary')
      );
  }

  close() {
    this.router.navigate(['/sharingOverview']);
  }

}
