import { Injectable } from '@angular/core';
import {Observable} from "rxjs/Observable";
import {Organisation} from "../organisation/models/Organisation";
import {ReportData} from "./models/ReportData";
import {HttpClient, HttpParams} from "@angular/common/http";

@Injectable()
export class ReportingService {

  constructor(private http: HttpClient) { }

  getPublisherReport(orgs: Organisation[], agreementName: string): Observable<ReportData[]> {

    let params = new HttpParams();
    for (let ix in orgs) {
      params = params.append('odsCodes', orgs[ix].odsCode);
    }
    params.set('agreementName', agreementName);
    return this.http.get<ReportData[]>('api/report/getPublisherReport', { params });
  }

  getActivityReport(parentMapTypeId: number, childMapTypeId: number, days: number): Observable<any[]> {

    let params = new HttpParams();
    params = params.append('parentMapTypeId', parentMapTypeId.toString());
    params = params.append('childMapTypeId', childMapTypeId.toString());
    params = params.append('days', days.toString());
    return this.http.get<any[]>('api/report/recentActivityReport', { params });
  }

}
