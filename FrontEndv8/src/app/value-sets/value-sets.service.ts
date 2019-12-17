import { Injectable } from '@angular/core';
import {Observable} from "rxjs/Observable";
import {HttpClient, HttpParams} from "@angular/common/http";
import {ValueSets} from "../models/ValueSets";

@Injectable({
  providedIn: 'root'
})
export class ValueSetsService {

  constructor(private http: HttpClient) {  }

  search(searchData: string, pageNumber: number = 1, pageSize: number = 20,
         orderColumn: string = 'name', descending: boolean = false ): Observable<ValueSets[]> {
    let params = new HttpParams();

    if (searchData) params = params.append('searchData', searchData);
    if (pageNumber) params = params.append('pageNumber', pageNumber.toString());
    if (pageSize) params = params.append('pageSize', pageSize.toString());
    if (orderColumn) params = params.append('orderColumn', orderColumn);
    if (descending) params = params.append('descending', descending.toString());
    return this.http.get<ValueSets[]>('api/value_sets', {params});
  }

  getTotalCount(expression: string): Observable<number> {
    let params = new HttpParams();
    if (expression) params = params.append('expression', expression);
    return this.http.get<number>('api/value_sets/searchCount', {params});
  }

  delete(valueSets?: ValueSets[]): Observable<any> {
    let params = new HttpParams();
    for(let i = 0; i < valueSets.length; i++){
      params = params.append("ids", valueSets[i].id.toString());
    }
    return this.http.delete<ValueSets[]>('api/value_sets/', {params});
  }
}
