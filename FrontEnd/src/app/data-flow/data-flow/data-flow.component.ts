import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataFlowService} from '../data-flow.service';
import {DataFlow} from '../models/DataFlow';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {Router} from '@angular/router';
import {ToastsManager} from 'ng2-toastr';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-data-flow',
  templateUrl: './data-flow.component.html',
  styleUrls: ['./data-flow.component.css']
})
export class DataFlowComponent implements OnInit {
  private paramSubscriber: any;
  dataflows: DataFlow[];
  allowEdit = false;
  loadingComplete = false;

  public activeProject: UserProject;

  dataflowDetailsToShow = new DataFlow().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dataFlowService: DataFlowService,
              private securityService: SecurityService,
              private log: LoggerService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
    this.getDataFlows();
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
  }

  getDataFlows() {
    const vm = this;
    vm.loadingComplete = false;
    vm.dataFlowService.getAllDataFlows()
      .subscribe(
        result => {
          vm.dataflows = result;
          vm.loadingComplete = true;
        },
            error => {
          vm.log.error('The data flows could not be loaded. Please try again.', error, 'Load data flows');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dataFlow', 1, 'add']);
  }

  edit(item: DataFlow) {
    this.router.navigate(['/dataFlow', item.uuid, 'edit']);
  }

  delete(item: DataFlow) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete data flow', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data flow', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: DataFlow) {
    const vm = this;
    vm.dataFlowService.deleteDataFlow(item.uuid)
      .subscribe(
        () => {
          const index = vm.dataflows.indexOf(item);
          vm.dataflows.splice(index, 1);
          vm.log.success('Data flow deleted', item, 'Delete data flow');
        },
        (error) => vm.log.error('The data flow could not be deleted. Please try again.', error, 'Delete data flow')
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
