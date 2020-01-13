import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {DataSharingSummary} from './models/DataSharingSummary';

@Injectable()
export class DataSharingSummaryService {

  constructor(private http: HttpClient) { }

  getAllDataSharingSummaries(): Observable<DataSharingSummary[]> {
    const url = 'api/dataSharingSummary';
    return this.http.get<DataSharingSummary[]>(url);
  }

  getDataSharingSummary(uuid: string): Observable<DataSharingSummary> {
    const url = 'api/dataSharingSummary';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataSharingSummary>(url,{params});
  }

  saveDataSharingSummary(cohort: DataSharingSummary): Observable<any> {
	//const httpOptions = { responseType: 'text' };
    const url = 'api/dataSharingSummary';
    return this.http.post<any>(url, cohort);
  }

  deleteDataSharingSummary(uuid: string): Observable<any> {
    const url = 'api/dataSharingSummary';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.delete<any>(url, {params});
  }

  search(searchData: string): Observable<DataSharingSummary[]> {
    const url = 'api/dataSharingSummary';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<DataSharingSummary[]>(url,{params});
  }

}
