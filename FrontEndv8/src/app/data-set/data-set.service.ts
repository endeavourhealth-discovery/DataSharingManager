import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {DataSet} from './models/Dataset';
import {Dpa} from '../data-processing-agreement/models/Dpa';
import {Dsa} from "../data-sharing-agreement/models/Dsa";
import {Project} from "../project/models/Project";
import {Region} from "../region/models/Region";

@Injectable()
export class DataSetService {

  constructor(private http: HttpClient) { }

  getAllDataSets(): Observable<DataSet[]> {
    const url = 'api/dataSet';
    return this.http.get<DataSet[]>(url);
  }

  getDataSetsBasedOnRegion(userId: string): Observable<DataSet[]> {
    const url = 'api/dataSet';
    let params = new HttpParams();
    if (userId) params = params.append('userId', userId);
    return this.http.get<DataSet[]>(url, {params});
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

  getLinkedDsas(uuid: string):  Observable<Dsa[]> {
    const url = 'api/dataSet/dsas';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa[]>(url, {params});
  }

  getLinkedProjects(uuid: string):  Observable<Project[]> {
    const url = 'api/dataSet/projects';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Project[]>(url, {params});
  }

  getLinkedRegions(uuid: string, userId: string):  Observable<Region[]> {
    const url = 'api/dataSet/regions';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Region[]>(url,{params});
  }

  updateMappings(dataset: DataSet): Observable<any> {
    const url = 'api/dataSet/updateMappings';
    return this.http.post(url, dataset, { responseType: 'text' });
  }
}
