import {Component, OnInit} from '@angular/core';
import {MatTableDataSource} from '@angular/material';
import {ConceptService} from '../concept.service';
import {LoggerService, StateService} from 'dds-angular8';
import {IMModel} from '../../models/IMModel';
import {PageEvent} from '@angular/material/paginator';
import {SearchResult} from '../../models/SearchResult';
import {ConceptSummary} from '../../models/ConceptSummary';

@Component({
  selector: 'app-concept-library',
  templateUrl: './concept-library.component.html',
  styleUrls: ['./concept-library.component.scss']
})
export class ConceptLibraryComponent implements OnInit {
  concepts: any;
  searchTerm: string;
  dataSource: MatTableDataSource<ConceptSummary>;
  models: IMModel[];
  statusFilter: string[];
  modelFilter: string[];
  page: number = 0;
  size: number = 15;

  displayedColumns: string[] = ['id', 'name', 'scheme', 'code'];

  constructor(
    private conceptService: ConceptService,
    private log: LoggerService,
    private state: StateService
    ) { }

  ngOnInit() {
    this.loadModels();
    this.loadState();
    this.search()
  }

  clear() {
    this.searchTerm = '';
    this.loadMRU();
  }

  loadMRU() {
    this.state.set('ConceptLibraryComponent', null);
    this.concepts = null;
    this.conceptService.getMRU(this.size)
      .subscribe(
        (result) => this.displayConcepts(result),
        (error) => this.log.error(error)
      );
  }

  loadModels() {
    this.conceptService.getModels()
      .subscribe(
        (models) => this.models = models,
        (error) => this.log.error(error)
      );
  }

  displayConcepts(concepts: SearchResult) {
    this.concepts = concepts;
    this.dataSource = new MatTableDataSource(concepts.results);
  }

  search() {
    this.saveState();

    if (!this.searchTerm)
      this.loadMRU();
    else {
      this.concepts = null;
      this.dataSource = null;
      this.conceptService.search({term: this.searchTerm, page: this.page, size: this.size}).subscribe(
        (result) => this.displayConcepts(result),
        (error) => this.log.error(error)
      );
    }
  }

  onPage(event: PageEvent) {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.search();
  }

  private loadState(): boolean {
    let data = this.state.get('ConceptLibraryComponent');
    if (data) {
      this.searchTerm = data.term;
      this.page = data.page;
      this.size = data.size;
      this.modelFilter = data.modelFilter;
      this.statusFilter = data.statusFilter;
      return true;
    } else
      return false;
  }

  private saveState() {
    this.state.set('ConceptLibraryComponent', {
      term: this.searchTerm,
      page: this.page,
      size: this.size,
      modelFilter: this.modelFilter,
      statusFilter: this.statusFilter
    });
  }
}
