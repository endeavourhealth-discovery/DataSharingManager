import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";

@Component({
  selector: 'app-generic-table',
  templateUrl: './generic-table.component.html',
  styleUrls: ['./generic-table.component.scss']
})
export class GenericTableComponent implements OnInit {
  @Input() items : any[] = [];
  @Input() typeDescription : string = '';
  @Input() model : string = '';
  @Input() primary : string = 'name';
  @Input() primaryOrderText : string = this.primary;
  @Input() detailsToShow : any[] = [];
  @Input() displayClass : string = 'region';
  @Input() secondary : string;
  @Input() secondaryOrderText : string = this.secondary;
  @Input() pageSize : number = 12;
  @Input() allowDelete : boolean = false;
  @Input() allowEdit : boolean = false;
  @Input() noLink : boolean = false;
  @Input() noSearch: boolean = false;
  @Input() showEditButton: boolean = true;

  @Output() deleted: EventEmitter<string> = new EventEmitter<string>();
  @Output() clicked: EventEmitter<string> = new EventEmitter<string>();
  @Output() onshowPicker: EventEmitter<string> = new EventEmitter<string>();


  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  propertiesToShow: any[] = [];

  public filterText : string = "";
  dataSource: any;
  selection = new SelectionModel<any>(true, []);

  constructor() {

  }

  ngOnInit() {
    this.propertiesToShow = this.detailsToShow.map(x => x.property);
    this.propertiesToShow.push('select');
  }

  ngOnChanges(changes) {
    this.dataSource = new MatTableDataSource(this.items);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  clear() {
    this.filterText = '';
    this.applyFilter('');
  }

  delete() {
    console.log('delete presses');
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
