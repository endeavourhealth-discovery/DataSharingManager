import {Component, Input, OnInit} from '@angular/core';
import {AttributeExpression} from '../../models/AttributeExpression';
import {ConceptService} from '../concept.service';
import {LoggerService} from 'dds-angular8';

@Component({
  selector: 'attribute-expression',
  templateUrl: './attribute-expression.component.html',
  styleUrls: ['./attribute-expression.component.scss']
})
export class AttributeExpressionComponent implements OnInit {
  @Input() attributeExpression: AttributeExpression;

  constructor(private conceptService: ConceptService, private log: LoggerService) { }

  ngOnInit() {
  }

  getText(conceptId: string) {
    return this.conceptService.getName(conceptId);
  }

}
