import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Concept} from '../../models/Concept';
import {MatTreeNestedDataSource} from '@angular/material/tree';
import {NestedTreeControl} from '@angular/cdk/tree';
import {ConceptService} from '../concept.service';
import {LoggerService} from 'dds-angular8';
import {ConceptTreeNode} from '../../models/ConceptTreeNode';

@Component({
  selector: 'app-parent-hierarchy-dialog',
  templateUrl: './parent-hierarchy-dialog.component.html',
  styleUrls: ['./parent-hierarchy-dialog.component.scss']
})
export class ParentHierarchyDialogComponent implements OnInit {
  concept: Concept;
  treeControl = new NestedTreeControl<ConceptTreeNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<ConceptTreeNode>();

  constructor(
    private conceptService: ConceptService,
    private logger: LoggerService,
    public dialogRef: MatDialogRef<ParentHierarchyDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: Concept) {
    this.concept = data;
  }

  ngOnInit() {
    this.conceptService.getParentHierarchy(this.concept.concept.id).subscribe(
      (hierarchy) => this.display(hierarchy),
      (error) => this.logger.error(error)
    );
  }

  display(hierarchy: ConceptTreeNode[]) {
    this.dataSource.data = hierarchy;
    this.treeControl.dataNodes = hierarchy;
    this.treeControl.expandAll();
  }

  close() {
    this.dialogRef.close();
  }

  hasChild = (_: number, node: ConceptTreeNode) => !!node.children && node.children.length > 0;
}
