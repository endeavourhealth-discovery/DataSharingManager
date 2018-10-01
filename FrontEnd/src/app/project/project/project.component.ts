import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Project} from "../models/Project";
import {ToastsManager} from "ng2-toastr";
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from "eds-angular4";
import {Router} from "@angular/router";
import {ProjectService} from "../project.service";
import {DataFlow} from "../../data-flow/models/DataFlow";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.css']
})
export class ProjectComponent implements OnInit {
  projects: Project[];
  allowEdit = false;
  loadingComplete = false;

  public activeProject: UserProject;

  projectDetailsToShow = new Project().getDisplayItems();

  constructor(private $modal: NgbModal,
              private projectService: ProjectService,
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

    this.getProjects();
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
  }

  getProjects() {
    const vm = this;
    vm.loadingComplete = false;
    vm.projectService.getAllProjects()
      .subscribe(
        result => {
          vm.projects = result;
          vm.loadingComplete = true;
        },
        error => {
          vm.log.error('The projects could not be loaded. Please try again.', error, 'Load projects');
          vm.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/project', 1, 'add']);
  }

  edit(item: DataFlow) {
    this.router.navigate(['/project', item.uuid, 'edit']);
  }

  delete(item: Project) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete project', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete project', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Project) {
    const vm = this;
    vm.projectService.deleteProject(item.uuid)
      .subscribe(
        () => {
          const index = vm.projects.indexOf(item);
          vm.projects.splice(index, 1);
          vm.log.success('Project deleted', item, 'Delete project');
        },
        (error) => vm.log.error('The project could not be deleted. Please try again.', error, 'Delete project')
      );
  }

  close() {
    this.router.navigate(['/sharingOverview']);
  }

}
