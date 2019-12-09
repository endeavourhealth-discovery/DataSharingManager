import { Injectable } from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Concept} from '../models/Concept';
import {IMModel} from '../models/IMModel';
import {ConceptTreeNode} from '../models/ConceptTreeNode';
import {SearchResult} from '../models/SearchResult';

@Injectable({
  providedIn: 'root'
})
export class ConceptService {
  private _nameCache: any = {};

  constructor(private http: HttpClient) { }

  getMRU(size?: number): Observable<SearchResult> {
    let params = new HttpParams();
    if (size) params = params.append('size', size.toString());

    return this.http.get<SearchResult>('api/concepts/mru', {params});
  }

  getModels(): Observable<IMModel[]> {
    return this.http.get<IMModel[]>('api/models');
  }

  getConcept(conceptId: string): Observable<Concept> {
    return this.http.get<Concept>('api/concepts/' + conceptId);
  }

  search(args: {term: string, size?: number, page?: number, models?: any[], relationship?: string, target?: string}): Observable<any> {
    let params = new HttpParams();
    params = params.append('term', args.term);

    if (args.size) params = params.append('size', args.size.toString());
    if (args.page) params = params.append('page', args.page.toString());
    if (args.models) {
      for (let model of args.models)
        params = params.append('model', model);
    }
    if (args.relationship) params = params.append('relationship', args.relationship);
    if (args.target) params = params.append('target', args.target);

    return this.http.get('api/concepts/search', {params});
  }

  getName(conceptId: string) {
    if (!this._nameCache[conceptId]) {

      this._nameCache[conceptId] = conceptId;

      this.http.get('api/concepts/' + conceptId + '/name', {responseType: 'text'})
        .subscribe(
          (response) => this._nameCache[conceptId] = ( response || conceptId),
          (error) => console.error(error)
        );
    }
    return this._nameCache[conceptId];
  }

  getParentHierarchy(conceptId: string): Observable<ConceptTreeNode[]> {
    return this.http.get<ConceptTreeNode[]>('api/concepts/' + conceptId + '/parentTree');
  }

  getChildren(conceptId: string): Observable<ConceptTreeNode[]> {
    return this.http.get<ConceptTreeNode[]>('api/concepts/' + conceptId + '/children');
  }
}
