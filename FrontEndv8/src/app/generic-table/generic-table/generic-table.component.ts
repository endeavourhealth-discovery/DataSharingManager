import {Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTable, MatTableDataSource} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";
import {animate, state, style, transition, trigger} from "@angular/animations";

@Component({
  selector: 'app-generic-table',
  templateUrl: './generic-table.component.html',
  styleUrls: ['./generic-table.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class GenericTableComponent implements OnInit, OnChanges {
  @Input() items: any[] = [];
  @Input() detailsToShow: any[] = [];
  @Input() pageSize = 20;
  @Input() allowSelect = false;
  @Input() allowSearch = true;
  @Input() allowExpand = true;

  @Output() clicked: EventEmitter<any> = new EventEmitter<any>();

  @ViewChild(MatTable, { static: false }) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
  propertiesToShow: string[] = [];
  expandedToShow: any[] = [];
  primaryToShow: any[] = [];
  expandedElement: null;

  public filterText = '';
  filtered  = false;
  dataSource: any;
  selection = new SelectionModel<any>(true, []);
  highlightedRows: any;

  constructor() {

  }

  clearHighlights() {
    this.highlightedRows = null;
  }

  updateRows() {
    this.dataSource = new MatTableDataSource(this.items);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    if (this.table) {
      this.table.renderRows();
    }
  }

  ngOnInit() {

    this.expandedToShow = this.detailsToShow.filter(det => det.secondary);
    this.primaryToShow = this.detailsToShow.filter(det => !det.secondary);
    this.propertiesToShow = this.primaryToShow.map(x => x.property);

  }

  ngOnChanges(changes) {

    this.updateRows();
    var selectIndex = this.propertiesToShow.indexOf('select');
    var actionIndex: number = this.propertiesToShow.indexOf('action');

    // only allow items to be selected if user has admin rights
    if (this.allowSelect) {
      if (selectIndex < 0) {
        this.propertiesToShow.unshift('select');
      }
    } else {
      if (selectIndex > -1) {
        this.propertiesToShow.splice(selectIndex, 1);
      }
    }

    if (this.allowExpand && this.expandedToShow.length > 0) {
      if (actionIndex < 0) {
        this.propertiesToShow.push('action');
      }
    }
  }

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim().toLowerCase();

    this.filtered = true;

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  expand(event, item: any) {
    event.stopPropagation();
    this.expandedElement = this.expandedElement === item ? null : item;
  }

  clear() {
    this.filterText = '';
    this.filtered = false;
    this.applyFilter('');
  }

  clickItem(row: any, e: any) {
    this.highlightedRows = row;
    console.log(e.target.className);
    if (!e.target.className.includes('mat-column-select') && !e.target.className.includes('mat-column-action')) {
      this.clicked.emit(row);
    }
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.selection.select(row));
  }

  /** The label for the checkbox on the passed row */
  checkboxLabel(row?: any): string {
    if (!row) {
      return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${row.position + 1}`;
  }

}
