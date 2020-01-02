import {Component, OnInit} from '@angular/core';
import {DataSharingSummary} from '../models/DataSharingSummary';
import {DataSharingSummaryService} from '../data-sharing-summary.service';
import {Router} from '@angular/router';
import {LoggerService} from "dds-angular8";

@Component({
  selector: 'app-data-sharing-summary',
  templateUrl: './data-sharing-summary.component.html',
  styleUrls: ['./data-sharing-summary.component.css']
})
export class DataSharingSummaryComponent implements OnInit {
  dataSharingSummaries: DataSharingSummary[] = [];
  allowDelete = true;
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  dssDetailsToShow = new DataSharingSummary().getDisplayItems();
  //loadingComplete = false;

  constructor(private dataSharingSummaryService: DataSharingSummaryService,
              private log: LoggerService,
              private router: Router,
              ) {
  }

  ngOnInit() {
    this.getDataSharingSummaries();
  }

  getDataSharingSummaries() {
    this.dataSharingSummaryService.getAllDataSharingSummaries()
      .subscribe(
        result => this.dataSharingSummaries = result,
        error => this.log.error('The data sharing summaries could not be loaded. Please try again.'/*, error, 'Load data sharing summaries'*/)
      );
  }

  add() {
    this.router.navigate(['/dataSharingSummary', 1, 'add']);
  }

  edit(item: DataSharingSummary) {
    this.router.navigate(['/dataSharingSummary', item.uuid, 'edit']);
  }

  itemClicked(dataSharingSummary: DataSharingSummary) {
    this.router.navigate(['/dataSharingSummary', dataSharingSummary.uuid, 'edit']);
  }

  delete(item: DataSharingSummary) {
    /*MessageBoxDialog.open(this.$modal, 'Delete data sharing summary', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data sharing summary', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: DataSharingSummary) {
    this.dataSharingSummaryService.deleteDataSharingSummary(item.uuid)
      .subscribe(
        () => {
          const index = this.dataSharingSummaries.indexOf(item);
          this.dataSharingSummaries.splice(index, 1);
          this.log.success('Data sharing summary deleted'/*, item, 'Delete data sharing summary'*/);
        },
        (error) => this.log.error('The data sharing summary could not be deleted. Please try again.'/*, error, 'Delete data sharing summary'*/)
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
