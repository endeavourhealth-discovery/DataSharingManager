import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataSharingSummary} from '../models/DataSharingSummary';
import {DataSharingSummaryService} from '../data-sharing-summary.service';
import {LoggerService} from 'eds-angular4';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ToastsManager} from 'ng2-toastr';

@Component({
  selector: 'app-data-sharing-summary-editor',
  templateUrl: './data-sharing-summary-editor.component.html',
  styleUrls: ['./data-sharing-summary-editor.component.css']
})
export class DataSharingSummaryEditorComponent implements OnInit {
  private paramSubscriber: any;
  dataSharingSummary: DataSharingSummary = <DataSharingSummary>{};

  NatureOfInformation = [
    {num: 0, name : 'Personal'},
    {num: 1, name : 'Personal Sensitive'},
    {num: 2, name : 'Commercial'}
  ];

  FormatTypes = [
    {num: 0, name : 'Removable Media'},
    {num: 1, name : 'Electronic Structured Data'}
  ];

  DataSubjectTypes = [
    {num: 0, name : 'Patient'}
  ];

  ReviewCycles = [
    {num: 0, name : 'Annually'},
    {num: 1, name : 'Monthly'},
    {num: 2, name : 'Weekly'}
  ];

  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private dataSharingSummaryService: DataSharingSummaryService,
              private router: Router,
              private route: ActivatedRoute,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode'], params['id']);
      });
  }

  protected performAction(action: string, itemUuid: string) {
    switch (action) {
      case 'add':
        this.create(itemUuid);
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  create(uuid: string) {
    this.dataSharingSummary = {
      name : ''
    } as DataSharingSummary;
  }

  load(uuid: string) {
    const vm = this;
    vm.dataSharingSummaryService.getDataSharingSummary(uuid)
      .subscribe(result =>  {
          vm.dataSharingSummary = result;
        },
        error => vm.log.error('The data sharing summary could not be loaded. Please try again.', error, 'Load data sharing summary')
      );
  }

  save(close: boolean) {
    const vm = this;
    console.log(vm.dataSharingSummary);

    vm.dataSharingSummaryService.saveDataSharingSummary(vm.dataSharingSummary)
      .subscribe(saved => {
          vm.dataSharingSummary.uuid = saved;
          vm.log.success('Data sharing summary saved', vm.dataSharingSummary, 'Save data sharing summary');
          if (close) { this.router.navigate(['/overview']); }
        },
        error => vm.log.error('The data sharing summary could not be saved. Please try again.', error, 'Save data sharing summary')
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
