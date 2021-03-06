import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Organisation} from '../../organisation/models/Organisation';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {RegionService} from '../region.service';
import {Region} from '../models/Region';
import {Router} from '@angular/router';
import {ToastsManager} from 'ng2-toastr';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

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

  constructor(private $modal: NgbModal,
              private regionService: RegionService,
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

    this.getRegions(vm.userId);
  }

  getRegions(userId: string) {
    const vm = this;
    vm.loadingComplete = false;
    vm.regionService.getAllRegions(userId)
      .subscribe(
        result => {
          vm.regions = result;
          vm.loadingComplete = true;
        },
        error => {
          vm.log.error('The regions could not be loaded. Please try again.', error, 'Load regions');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/region', 1, 'add']);
  }

  edit(item: Region) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  delete(item: Region) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete region', 'Are you sure that you want to delete the <b>' + item.name + '</b> region?', 'Delete region', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Region) {
    const vm = this;
    vm.regionService.deleteRegion(item.uuid)
      .subscribe(
        () => {
          const index = vm.regions.indexOf(item);
          vm.regions.splice(index, 1);
          vm.log.success('Region deleted', item, 'Delete region');
        },
        (error) => vm.log.error('The region could not be deleted. Please try again.', error, 'Delete region')
      );
  }

  close() {
    this.router.navigate(['overview']);
  }

}
