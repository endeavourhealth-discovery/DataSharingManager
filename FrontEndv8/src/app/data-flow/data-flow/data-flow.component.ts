import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataFlow} from '../models/DataFlow';
import {DataFlowService} from '../data-flow.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";

@Component({
  selector: 'app-data-flow',
  templateUrl: './data-flow.component.html',
  styleUrls: ['./data-flow.component.css']
})
export class DataFlowComponent implements OnInit {
  dataflows: DataFlow[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  dataflowDetailsToShow = new DataFlow().getDisplayItems();
  loadingComplete = false;

  public activeProject: UserProject;

  constructor(private dataFlowService: DataFlowService,
              private router: Router,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
    this.getDataFlows();
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
    }
  }

  getDataFlows() {
    this.loadingComplete = false;
    this.dataFlowService.getAllDataFlows()
      .subscribe(
        result => {
          this.dataflows = result;
          this.loadingComplete = true;
        },
            error => {
          this.log.error('The data flows could not be loaded. Please try again.'/*, error, 'Load data flows'*/);
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dataFlow', 1, 'add']);
  }

  edit(item: DataFlow) {
    this.router.navigate(['/dataFlow', item.uuid, 'edit']);
  }

  itemClicked(dataFlow: DataFlow) {
    this.router.navigate(['/dataFlow', dataFlow.uuid, 'edit']);
  }

  delete(item: DataFlow) {
    /*MessageBoxDialog.open(vm.$modal, 'Delete data flow', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data flow', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: DataFlow) {
    this.dataFlowService.deleteDataFlow(item.uuid)
      .subscribe(
        () => {
          const index = this.dataflows.indexOf(item);
          this.dataflows.splice(index, 1);
          this.log.success('Data flow deleted'/*, item, 'Delete data flow'*/);
        },
        (error) => this.log.error('The data flow could not be deleted. Please try again.'/*, error, 'Delete data flow'*/)
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
