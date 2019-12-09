import {Injectable} from '@angular/core';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {FlatTreeControl} from '@angular/cdk/tree';
import {CollectionViewer, SelectionChange} from '@angular/cdk/collections';
import {map} from 'rxjs/operators';
import {ConceptTreeNode} from '../../models/ConceptTreeNode';
import {ConceptService} from '../concept.service';
import {LoggerService} from 'dds-angular8';

@Injectable()
export class DynamicDataSource {

  dataChange = new BehaviorSubject<ConceptTreeNode[]>([]);

  get data(): ConceptTreeNode[] { return this.dataChange.value; }
  set data(value: ConceptTreeNode[]) {
    this._treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  constructor(private _treeControl: FlatTreeControl<ConceptTreeNode>,
              private conceptService: ConceptService,
              private logger: LoggerService) {}

  connect(collectionViewer: CollectionViewer): Observable<ConceptTreeNode[]> {
    this._treeControl.expansionModel.onChange.subscribe(change => {
      if ((change as SelectionChange<ConceptTreeNode>).added ||
        (change as SelectionChange<ConceptTreeNode>).removed) {
        this.handleTreeControl(change as SelectionChange<ConceptTreeNode>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<ConceptTreeNode>) {
    if (change.added) {
      change.added.forEach(node => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed.slice().reverse().forEach(node => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, remove from display list
   */
  toggleNode(node: ConceptTreeNode, expand: boolean) {
    const index = this.data.indexOf(node);
    if (index < 0)
      return;

    if (!expand) {
      let count = 0;
      for (let i = index + 1; i < this.data.length
      && this.data[i].level > node.level; i++, count++) {}
      this.data.splice(index + 1, count);
      this.dataChange.next(this.data);
    } else {
      node.isLoading = true;
      this.conceptService.getChildren(node.id).subscribe(
        (children: ConceptTreeNode[]) => {
          children.forEach(c => {
            c.level = node.level + 1;
            c.expandable = true;
          });
          this.data.splice(index + 1, 0, ...children);
          this.dataChange.next(this.data);
          node.isLoading = false;
        },
        (error) => this.logger.error(error)
      );
    }
  }
}
