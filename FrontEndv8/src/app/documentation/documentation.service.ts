import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {Documentation} from './models/Documentation';

@Injectable()
export class DocumentationService {

  constructor(private http: HttpClient) { }

  getDocument(uuid: string): Observable<Documentation> {
    const url = 'api/documentation';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Documentation>(url, {params});
  }

  getAllAssociatedDocuments(parentUuid: string, parentType: string ): Observable<Documentation[]> {
    const url = 'api/documentation/associated';
    let params = new HttpParams();
    if (parentUuid) params = params.append('parentUuid', parentUuid);
    if (parentType) params = params.append('parentType', parentType);
    return this.http.get<Documentation[]>(url, {params});
  }

  deleteDocument(uuid: string): Observable<any> {
    const url = 'api/documentation';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.delete<any>(url, {params});
  }

}
