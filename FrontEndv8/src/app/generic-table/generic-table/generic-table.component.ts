import {Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTable, MatTableDataSource} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";

@Component({
  selector: 'app-generic-table',
  templateUrl: './generic-table.component.html',
  styleUrls: ['./generic-table.component.scss']
})
export class GenericTableComponent implements OnInit, OnChanges {
  @Input() items : any[] = [];
  @Input() detailsToShow : any[] = [];
  @Input() pageSize : number = 20;
  @Input() allowSelect : boolean = false;

  @Output() clicked: EventEmitter<any> = new EventEmitter<any>();

  @ViewChild(MatTable, { static: false }) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  propertiesToShow: string[] = [];

  public filterText : string = '';
  filtered  = false;
  dataSource: any;
  selection = new SelectionModel<any>(true, []);

  constructor() {

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
    this.propertiesToShow = this.detailsToShow.map(x => x.property);
  }

  ngOnChanges(changes) {

    this.updateRows();
    var selectIndex: number = this.propertiesToShow.indexOf('select');

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
  }

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim().toLowerCase();

    this.filtered = true;

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  clear() {
    this.filterText = '';
    this.filtered = false;
    this.applyFilter('');
  }

  clickItem(row: any) {
    this.clicked.emit(row);
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
