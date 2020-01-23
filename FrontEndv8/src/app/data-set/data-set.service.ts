import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {DataSet} from './models/Dataset';
import {Dpa} from '../data-processing-agreement/models/Dpa';

@Injectable()
export class DataSetService {

  constructor(private http: HttpClient) { }

  getAllDataSets(): Observable<DataSet[]> {
    const url = 'api/dataSet';
    return this.http.get<DataSet[]>(url);
  }

  getDataSet(uuid: string): Observable<DataSet> {
    const url = 'api/dataSet';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataSet>(url, {params});
  }

  saveDataSet(dataset: DataSet): Observable<any> {
    const url = 'api/dataSet';
    return this.http.post(url, dataset, { responseType: 'text' });
  }

  deleteDataSet(uuid: string[]): Observable<any> {
    const url = 'api/dataSet';
    let params = new HttpParams();
    for (let ix in uuid) {
      params = params.append('uuids', uuid[ix]);
    }
    return this.http.delete<any>(url, {params});
  }

  search(searchData: string): Observable<DataSet[]> {
    const url = 'api/dataSet';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<DataSet[]>(url, {params});
  }

  getLinkedDpas(uuid: string):  Observable<Dpa[]> {
    const url = 'api/dataSet/dpas';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dpa[]>(url, {params});
  }

  updateDPAMapping(dataset: DataSet): Observable<any> {
    const url = 'api/dataSet/updateDPAMapping';
    return this.http.post(url, dataset, { responseType: 'text' });
  }
}
