import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {Dsa} from '../models/Dsa';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
import {ToastsManager} from 'ng2-toastr';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-data-sharing-agreement',
  templateUrl: './data-sharing-agreement.component.html',
  styleUrls: ['./data-sharing-agreement.component.css']
})
export class DataSharingAgreementComponent implements OnInit {
  dsas: Dsa[];
  allowEdit = false;
  superUser = false;
  loadingComplete = false;
  userId: string;

  public activeProject: UserProject;

  dsaDetailsToShow = new Dsa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private dsaService: DataSharingAgreementService,
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
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      vm.allowEdit = true;
      vm.superUser = true;
      vm.userId = null;
    } else if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
      vm.superUser = false;
      vm.userId = vm.activeProject.userId;
    } else {
      vm.allowEdit = false;
      vm.superUser = false;
      vm.userId = vm.activeProject.userId;
    }
    this.getDsas(vm.userId);
  }

  getDsas(userId: string) {
    const vm = this;
    vm.loadingComplete = false;
    console.log(userId);
    vm.dsaService.getAllDsas(userId)
      .subscribe(
        result => {
          vm.dsas = result;
          vm.loadingComplete = true;
        },
        error => {
          vm.log.error('The data sharing agreements could not be loaded. Please try again.', error, 'Load data sharing agreements');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dsa', 1, 'add']);
  }

  edit(item: Dsa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  delete(item: Dsa) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete data sharing agreement', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data sharing agreement', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Dsa) {
    const vm = this;
    vm.dsaService.deleteDsa(item.uuid)
      .subscribe(
        () => {
          const index = vm.dsas.indexOf(item);
          vm.dsas.splice(index, 1);
          vm.log.success('Data sharing agreement deleted', item, 'Delete data sharing agreement');
        },
        (error) => vm.log.error('The data sharing agreement could not be deleted. Please try again.', error, 'Delete data sharing agreement')
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
