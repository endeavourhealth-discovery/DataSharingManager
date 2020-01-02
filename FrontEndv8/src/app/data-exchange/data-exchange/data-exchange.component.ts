import {Component, OnInit} from '@angular/core';
import {DataExchange} from '../models/DataExchange';
import {DataExchangeService} from '../data-exchange.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";

@Component({
  selector: 'app-data-exchange',
  templateUrl: './data-exchange.component.html',
  styleUrls: ['./data-exchange.component.css']
})
export class DataExchangeComponent implements OnInit {
  exchanges: DataExchange[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  dataExchangeDetailsToShow = new DataExchange().getDisplayItems();
  loadingComplete = false;

  public activeProject: UserProject;

  constructor(private dataExchangeService: DataExchangeService,
              private router: Router,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService,) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
    this.getDataExchanges();
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
    }
  }

  getDataExchanges() {
    this.loadingComplete = false;
    this.dataExchangeService.getAllDataExchanges()
      .subscribe(
        result => {
          this.exchanges = result;
          this.loadingComplete = true;
        },
            error => {
          this.log.error('The data exchanges could not be loaded. Please try again.'/*, error, 'Load data exchanges'*/);
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dataExchange', 1, 'add']);
  }

  edit(item: DataExchange) {
    this.router.navigate(['/dataExchange', item.uuid, 'edit']);
  }

  itemClicked(dataExchange: DataExchange) {
    this.router.navigate(['/dataExchange', dataExchange.uuid, 'edit']);
  }

  delete(item: DataExchange) {
    /*MessageBoxDialog.open(this.$modal, 'Delete data exchange', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data exchange', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: DataExchange) {
    this.dataExchangeService.deleteDataExchange(item.uuid)
      .subscribe(
        () => {
          const index = this.exchanges.indexOf(item);
          this.exchanges.splice(index, 1);
          this.log.success('Data exchange deleted'/*, item, 'Delete data exchange'*/);
        },
        (error) => this.log.error('The data exchange could not be deleted. Please try again.'/*, error, 'Delete data exchange'*/)
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
