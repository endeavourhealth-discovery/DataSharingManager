import {Component, Input, OnInit} from '@angular/core';
import {Project} from "../models/Project";
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {LoggerService} from "eds-angular4";
import {ProjectService} from "../project.service";

@Component({
  selector: 'app-project-picker',
  templateUrl: './project-picker.component.html',
  styleUrls: ['./project-picker.component.css']
})
export class ProjectPickerComponent implements OnInit {

  @Input() resultData: Project[];
  searchData: string;
  searchResults: Project[];

  public static open(modalService: NgbModal, projects: Project[]) {
    const modalRef = modalService.open(ProjectPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], projects);

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private projectService: ProjectService) { }

  ngOnInit() {
  }

  search() {
    const vm = this;
    if (vm.searchData.length < 3) {
      return;
    }
    vm.projectService.search(vm.searchData)
      .subscribe(
        (result) => vm.searchResults = result,
        (error) => vm.log.error(error)
      );
  }

  private addToSelection(match: Project) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      this.resultData.push(match);
    }
  }

  private removeFromSelection(match: Project) {
    const index = this.resultData.indexOf(match, 0);
    if (index > -1) {
      this.resultData.splice(index, 1);
    }
  }

  ok() {
    this.activeModal.close(this.resultData);
    console.log('OK Pressed');
  }

  cancel() {
    this.activeModal.dismiss('cancel');
    console.log('Cancel Pressed');
  }

}
