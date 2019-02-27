import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataProcessingAgreementService} from '../data-processing-agreement.service';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Dpa} from '../models/Dpa';
import {ToastsManager} from 'ng2-toastr';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-data-processing-agreement',
  templateUrl: './data-processing-agreement.component.html',
  styleUrls: ['./data-processing-agreement.component.css']
})
export class DataProcessingAgreementComponent implements OnInit {
  dpas: Dpa[];
  allowEdit = false;
  loadingComplete = false;

  public activeProject: UserProject;

  dpaDetailsToShow = new Dpa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dpaService: DataProcessingAgreementService,
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
    this.getDsas();
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
  }

  getDsas() {
    const vm = this;
    vm.loadingComplete = false;
    vm.dpaService.getAllDpas()
      .subscribe(
        result => {
          vm.dpas = result;
          vm.loadingComplete = true;
        },
            error => {
          vm.log.error('The data processing agreements could not be loaded. Please try again.', error, 'Load data processing agreements');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dpa', 1, 'add']);
  }

  edit(item: Dpa) {
    this.router.navigate(['/dpa', item.uuid, 'edit']);
  }

  delete(item: Dpa) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete data processing agreement',
      'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data processing agreement', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Dpa) {
    const vm = this;
    vm.dpaService.deleteDpa(item.uuid)
      .subscribe(
        () => {
          const index = vm.dpas.indexOf(item);
          vm.dpas.splice(index, 1);
          vm.log.success('Data processing agreement deleted', item, 'Delete data processing agreement');
        },
        (error) => vm.log.error('The data processing agreement could not be deleted. Please try again.', error, 'Delete data processing agreement')
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
