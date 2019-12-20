import {Component, OnInit} from '@angular/core';
import {RegionService} from '../region.service';
import {Router} from '@angular/router';
import {Region} from "../../models/Region";
import {Organisation} from "../../models/Organisation";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";

@Component({
  selector: 'app-region',
  templateUrl: './region.component.html',
  styleUrls: ['./region.component.css']
})
export class RegionComponent implements OnInit {
  organisations: Organisation[];
  regions: Region[] = [];
  allowEdit = false;
  superUser = false;
  userId: string;
  loadingComplete = false;

  public activeProject: UserProject;

  regionDetailsToShow = new Region().getDisplayItems();

  constructor(private regionService: RegionService,
              private log: LoggerService,
              private router: Router,
              private userManagerService: UserManagerService) {
  }

  ngOnInit() {
    this.userManagerService.onProjectChange.subscribe(active => {
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

    this.getRegions(this.userId);
  }

  getRegions(userId: string) {

    this.loadingComplete = false;
    this.regionService.getAllRegions(userId)
      .subscribe(
        result => {
          this.regions = result;
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The regions could not be loaded. Please try again.');
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/region', 1, 'add']);
  }

  edit(item: Region) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  itemClicked(region: Region) {
    this.router.navigate(['/region', region.uuid, 'edit']);
  }

  delete(item: Region) {

    /*MessageBoxDialog.open(this.$modal, 'Delete region', 'Are you sure that you want to delete the <b>' + item.name + '</b> region?', 'Delete region', 'Cancel')
      .result.then(
      () => this.doDelete(item),
      () => this.log.info('Delete cancelled')
    );*/
  }

  doDelete(item: Region) {

    this.regionService.deleteRegion(item.uuid)
      .subscribe(
        () => {
          const index = this.regions.indexOf(item);
          this.regions.splice(index, 1);
          this.log.success('Region deleted');
        },
        (error) => this.log.error('The region could not be deleted. Please try again.')
      );
  }

  close() {
    this.router.navigate(['overview']);
  }

}
