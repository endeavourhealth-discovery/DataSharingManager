import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

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

  propertiesToShow: any[] = [];

  constructor() {

  }

  ngOnInit() {
    this.propertiesToShow = this.detailsToShow.map(x => x.property);
  }

}
