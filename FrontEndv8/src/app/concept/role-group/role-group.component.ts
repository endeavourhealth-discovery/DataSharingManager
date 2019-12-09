import {Component, Input, OnInit} from '@angular/core';
import {ConceptService} from '../concept.service';
import {LoggerService} from 'dds-angular8';
import {RoleGroup} from '../../models/RoleGroup';

@Component({
  selector: 'role-group',
  templateUrl: './role-group.component.html',
  styleUrls: ['./role-group.component.scss']
})
export class RoleGroupComponent implements OnInit {
  @Input() roleGroup: RoleGroup;

  constructor(private conceptService: ConceptService, private log: LoggerService) { }

  ngOnInit() {
  }

  getText(conceptId: string) {
    return this.conceptService.getName(conceptId);
  }

}
