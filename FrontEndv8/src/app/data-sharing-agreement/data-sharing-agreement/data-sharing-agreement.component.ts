import {Component, OnInit} from '@angular/core';
import {Dsa} from '../models/Dsa';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";

@Component({
  selector: 'app-data-sharing-agreement',
  templateUrl: './data-sharing-agreement.component.html',
  styleUrls: ['./data-sharing-agreement.component.css']
})
export class DataSharingAgreementComponent implements OnInit {
  dsas: Dsa[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  dsaDetailsToShow = new Dsa().getDisplayItems();
  superUser = false;
  userId: string;
  loadingComplete = false;

  public activeProject: UserProject;

  constructor(private dsaService: DataSharingAgreementService,
              private router: Router,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      this.allowEdit = true;
      this.superUser = true;
      this.userId = null;
    } else if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
      this.superUser = false;
      this.userId = this.activeProject.userId;
    } else {
      this.allowEdit = false;
      this.superUser = false;
      this.userId = this.activeProject.userId;
    }
    this.getDsas(this.userId);
  }

  getDsas(userId: string) {
    this.loadingComplete = false;
    console.log(userId);
    this.dsaService.getAllDsas(userId)
      .subscribe(
        result => {
          this.dsas = result;
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The data sharing agreements could not be loaded. Please try again.'/*, error, 'Load data sharing agreements'*/);
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dsa', 1, 'add']);
  }

  edit(item: Dsa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  itemClicked(dsa: Dsa) {
    this.router.navigate(['/dpa', dsa.uuid, 'edit']);
  }

  delete(item: Dsa) {
    /*MessageBoxDialog.open(this.$modal, 'Delete data sharing agreement', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data sharing agreement', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: Dsa) {
    this.dsaService.deleteDsa(item.uuid)
      .subscribe(
        () => {
          const index = this.dsas.indexOf(item);
          this.dsas.splice(index, 1);
          this.log.success('Data sharing agreement deleted'/*, item, 'Delete data sharing agreement'*/);
        },
        (error) => this.log.error('The data sharing agreement could not be deleted. Please try again.'/*, error, 'Delete data sharing agreement'*/)
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
