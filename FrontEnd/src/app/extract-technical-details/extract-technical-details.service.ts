import { Injectable } from '@angular/core';
import {URLSearchParams, Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {ExtractTechnicalDetails} from './models/ExtractTechnicalDetails';

@Injectable()
export class ExtractTechnicalDetailsService {

  constructor(private http: Http) { }


  getExtractTechnicalDetails(uuid: string): Observable<ExtractTechnicalDetails> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/extractTechnicalDetails', { search : params })
      .map((response) => response.json());
  }

  getAssociatedExtractTechnicalDetails(parentUuid: string, parentType: string ): Observable<ExtractTechnicalDetails> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('parentUuid', parentUuid);
    params.set('parentType', parentType);
    return vm.http.get('api/extractTechnicalDetails/associated', { search : params })
      .map((response) => response.json());
  }

  deleteExtractTechnicalDetails(uuid: string): Observable<any> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.delete('api/extractTechnicalDetails', { search : params })
      .map((response) => response.text());
  }

}
