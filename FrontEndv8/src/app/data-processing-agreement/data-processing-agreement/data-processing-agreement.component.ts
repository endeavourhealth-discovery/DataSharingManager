import {Component, OnInit} from '@angular/core';
import {Dpa} from '../models/Dpa';
import {DataProcessingAgreementService} from '../data-processing-agreement.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";

@Component({
  selector: 'app-data-processing-agreement',
  templateUrl: './data-processing-agreement.component.html',
  styleUrls: ['./data-processing-agreement.component.css']
})
export class DataProcessingAgreementComponent implements OnInit {
  dpas: Dpa[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  dpaDetailsToShow = new Dpa().getDisplayItems();
  superUser = false;
  userId: string;
  loadingComplete = false;

  public activeProject: UserProject;

  constructor(private dpaService: DataProcessingAgreementService,
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
    this.getDpas(this.userId);
  }

  getDpas(userId: string) {
    this.loadingComplete = false;
    this.dpaService.getAllDpas(userId)
      .subscribe(
        result => {
          this.dpas = result;
          this.loadingComplete = true;
        },
            error => {
          this.log.error('The data processing agreements could not be loaded. Please try again.'/*, error, 'Load data processing agreements'*/);
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dpa', 1, 'add']);
  }

  edit(item: Dpa) {
    this.router.navigate(['/dpa', item.uuid, 'edit']);
  }

  itemClicked(dpa: Dpa) {
    this.router.navigate(['/dpa', dpa.uuid, 'edit']);
  }

  delete(item: Dpa) {
    /*MessageBoxDialog.open(this.$modal, 'Delete data processing agreement',
      'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete data processing agreement', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: Dpa) {
    this.dpaService.deleteDpa(item.uuid)
      .subscribe(
        () => {
          const index = this.dpas.indexOf(item);
          this.dpas.splice(index, 1);
          this.log.success('Data processing agreement deleted'/*, item, 'Delete data processing agreement'*/);
        },
        (error) => this.log.error('The data processing agreement could not be deleted. Please try again.'/*, error, 'Delete data processing agreement'*/)
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
