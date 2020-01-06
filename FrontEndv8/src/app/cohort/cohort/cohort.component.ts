import {Component, OnInit, ViewChild} from '@angular/core';
import {Cohort} from '../models/Cohort';
import {CohortService} from '../cohort.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";

@Component({
  selector: 'app-cohort',
  templateUrl: './cohort.component.html',
  styleUrls: ['./cohort.component.css']
})
export class CohortComponent implements OnInit {
  cohorts: Cohort[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  cohortDetailsToShow = new Cohort().getDisplayItems();
  loadingComplete = false;

  public activeProject: UserProject;

  @ViewChild('cohortsTable', {static: false}) cohortsTable: GenericTableComponent;

  constructor(private cohortService: CohortService,
              private router: Router,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

    this.getCohorts();
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
    }
  }

  getCohorts() {
    this.loadingComplete = false;
    this.cohortService.getAllCohorts()
      .subscribe(
        result => {
          this.cohorts = result;
          this.loadingComplete = true;
        },
            error => {
          this.log.error('The cohorts could not be loaded. Please try again.'/*, error, 'Load cohorts'*/);
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/cohort', 1, 'add']);
  }

  edit(item: Cohort) {
    this.router.navigate(['/cohort', item.uuid,  'edit']);
  }

  itemClicked(cohort: Cohort) {
    this.router.navigate(['/cohort', cohort.uuid, 'edit']);
  }

  /*delete(item: Cohort) {*/
  delete() {
      console.log(this.cohortsTable.selection.selected);
    /*MessageBoxDialog.open(this.$modal, 'Delete cohort', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete cohort', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: Cohort) {
    this.cohortService.deleteCohort(item.uuid)
      .subscribe(
        () => {
          const index = this.cohorts.indexOf(item);
          this.cohorts.splice(index, 1);
          this.log.success('Cohort deleted'/*, item, 'Delete cohort'*/);
        },
        (error) => this.log.error('The cohort could not be deleted. Please try again.'/*, error, 'Delete cohort'*/)
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
