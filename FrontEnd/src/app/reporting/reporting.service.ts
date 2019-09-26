import { Injectable } from '@angular/core';
import {Dpa} from "../data-processing-agreement/models/Dpa";
import {Observable} from "rxjs/Observable";
import {Http, URLSearchParams} from "@angular/http";
import {Organisation} from "../organisation/models/Organisation";
import {ReportData} from "./models/ReportData";

@Injectable()
export class ReportingService {

  constructor(private http: Http) { }

  getPublisherReport(orgs: Organisation[], agreementName: string): Observable<ReportData[]> {
    const vm = this;
    let params = new URLSearchParams();
    for (let ix in orgs) {
      params.append('odsCodes', orgs[ix].odsCode);
    }
    params.set('agreementName', agreementName);
    return vm.http.get('api/report/getPublisherReport', { search : params })
      .map((response) => response.json());
  }

}
