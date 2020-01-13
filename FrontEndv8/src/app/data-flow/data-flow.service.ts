import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {DataFlow} from './models/DataFlow';
import {Dpa} from '../data-processing-agreement/models/Dpa';
import {Dsa} from '../data-sharing-agreement/models/Dsa';
import {DataExchange} from "../data-exchange/models/DataExchange";
import {Organisation} from "../organisation/models/Organisation";

@Injectable()
export class DataFlowService {

  constructor(private http: HttpClient) { }

  getAllDataFlows(): Observable<DataFlow[]> {
    const url = 'api/dataFlow';
    return this.http.get<DataFlow[]>(url);
  }

  getDataFlow(uuid: string): Observable<DataFlow> {
    const url = 'api/dataFlow';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataFlow>(url, {params});
  }

  saveDataFlow(cohort: DataFlow): Observable<any> {
	//const httpOptions = { responseType: 'text' };
    const url = 'api/dataFlow';
    return this.http.post<any>(url, cohort);
  }

  deleteDataFlow(uuid: string): Observable<any> {
    const url = 'api/dataFlow';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.delete<any>(url, {params});
  }

  search(searchData: string): Observable<DataFlow[]> {
    const url = 'api/dataFlow';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<DataFlow[]>(url, {params});
  }

  getLinkedDpas(uuid: string):  Observable<Dpa[]> {
    const url = 'api/dataFlow/dpas';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dpa[]>(url, {params});
  }

  getLinkedDsas(uuid: string):  Observable<Dsa[]> {
    const url = 'api/dataFlow/dsas'
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa[]>(url, {params});
  }

  getLinkedExchanges(uuid: string):  Observable<DataExchange[]> {
    const url = 'api/dataFlow/exchanges'
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataExchange[]>(url, {params});
  }

  getLinkedPublishers(uuid: string):  Observable<Organisation[]> {
    const url = 'api/dataFlow/publishers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>(url, {params});
  }

  getLinkedSubscribers(uuid: string):  Observable<Organisation[]> {
    const url = 'api/dataFlow/subscribers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>(url, {params});
  }

}
