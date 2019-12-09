import {Component, Input, OnInit} from '@angular/core';
import {ConceptExpression} from '../../models/ConceptExpression';
import {ConceptService} from '../concept.service';
import {LoggerService} from 'dds-angular8';

@Component({
  selector: 'concept-expression',
  templateUrl: './concept-expression.component.html',
  styleUrls: ['./concept-expression.component.scss']
})
export class ConceptExpressionComponent implements OnInit {
  @Input() expression: ConceptExpression;

  constructor(private conceptService: ConceptService, private log: LoggerService) { }

  ngOnInit() {
  }

  getText(conceptId: string) {
    return this.conceptService.getName(conceptId);
  }
}
