import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {DataExchange} from './models/DataExchange';
import {DataFlow} from "../data-flow/models/DataFlow";

@Injectable()
export class DataExchangeService {

  constructor(private http: HttpClient) { }

  getAllDataExchanges(): Observable<DataExchange[]> {
    const url = 'api/dataExchange';
    return this.http.get<DataExchange[]>(url);
  }

  getDataExchange(uuid: string): Observable<DataExchange> {
    const url = 'api/dataExchange';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataExchange>(url, {params});
  }

  saveDataExchange(cohort: DataExchange): Observable<any> {
	const httpOptions = { responseType: 'text' };
    const url = 'api/dataExchange';
    return this.http.post<any>(url, cohort, httpOptions);
  }

  deleteDataExchange(uuid: string): Observable<any> {
    const url = 'api/dataExchange';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.delete<any>(url, {params});
  }

  search(searchData: string): Observable<DataExchange[]> {
    const url = 'api/dataExchange';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<DataExchange[]>(url, {params});
  }

  getLinkedDataFlows(uuid: string):  Observable<DataFlow[]> {
    const url = 'api/dataExchange/dataFlows';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataFlow[]>(url, {params});
  }

}
