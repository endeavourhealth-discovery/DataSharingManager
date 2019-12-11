import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {Organisation} from '../models/Organisation';
import {Region} from "../models/Region";
import {Dpa} from "../models/Dpa";
import {Dsa} from "../models/Dsa";
import {Address} from "../models/Address";
import {FileUpload} from "../models/FileUpload";
import {OrganisationType} from "../models/OrganisationType";

@Injectable()
export class OrganisationService  {

  constructor(private http: HttpClient) {  }

  getOrganisation(uuid: string): Observable<Organisation> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation>('api/organisation', {params});
  }

  search(searchData: string, searchType: string,
         pageNumber: number = 1, pageSize: number = 20,
         orderColumn: string = 'name', descending: boolean = false ): Observable<Organisation[]> {
    let params = new HttpParams();

    if (searchData) params = params.append('searchData', searchData);
    if (searchType) params = params.append('searchType', searchType);
    if (pageNumber) params = params.append('pageNumber', pageNumber.toString());
    if (pageSize) params = params.append('pageSize', pageSize.toString());
    if (orderColumn) params = params.append('orderColumn', orderColumn);
    if (descending) params = params.append('descending', descending.toString());
    return this.http.get<Organisation[]>('api/organisation', {params});
  }

  getTotalCount(expression: string, searchType: string): Observable<number> {
    let params = new HttpParams();
    if (expression) params = params.append('expression', expression);
    if (searchType) params = params.append('searchType', searchType);
    return this.http.get<number>('api/organisation/searchCount', {params});
  }

  getOrganisationRegions(uuid: string, userId: string):  Observable<Region[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    if (userId) params = params.append('userId', userId);

    return this.http.get<Region[]>('api/organisation/regions', {params});
  }


  getChildOrganisations(uuid: string):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>('api/organisation/childOrganisations', {params});
  }

  getParentOrganisations(uuid: string, isService: number):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    if (isService) params = params.append('isService', isService.toString());
    return this.http.get<Organisation[]>('api/organisation/parentOrganisations', {params});
  }

  getServices(uuid: string):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>('api/organisation/services', {params});
  }

  getDPAPublishing(uuid: string):  Observable<Dpa[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dpa[]>('api/organisation/dpasPublishing', {params});
  }

  getDPAPublishingFromList(uuid: string[]):  Observable<Dpa[]> {
    let params = new HttpParams();
    for (let ix in uuid) {
      params.append('uuids', uuid[ix]);
    }
    return this.http.get<Dpa[]>('api/organisation/dpasPublishingFromList', {params});
  }

  getDSAPublishing(uuid: string):  Observable<Dsa[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa[]>('api/organisation/dsasPublishing', {params});
  }

  getDSAPublishingFromList(uuid: string[]):  Observable<Dsa[]> {
    let params = new HttpParams();
    for (let ix in uuid) {
      params.append('uuids', uuid[ix]);
    }
    return this.http.get<Dsa[]>('api/organisation/dsasPublishingFromList', {params});
  }

  getDSASubscribing(uuid: string):  Observable<Dsa[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa[]>('api/organisation/dsasSubscribing', {params});
  }

  getDSASubscribingFromList(uuid: string[]):  Observable<Dsa[]> {
    let params = new HttpParams();
    for (let ix in uuid) {
      params.append('uuids', uuid[ix]);
    }
    return this.http.get<Dsa[]>('api/organisation/dsasSubscribingFromList', {params});
  }

  getOrganisationAddresses(uuid: string):  Observable<Address[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Address[]>('api/organisation/addresses', {params});
  }

  saveOrganisation(organisation: Organisation): Observable<any> {
    return this.http.post<any>('api/organisation', organisation);
  }

  deleteOrganisation(uuid: string): Observable<any> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.delete<any>('api/organisation', {params});
  }

  getUpdatedBulkOrganisations(): Observable<any> {
    return this.http.get<any>('api/organisation/editedBulks');
  }

  getConflictedOrganisations(): Observable<any> {
    return this.http.get<any>('api/organisation/conflicts');
  }

  deleteBulks(): Observable<any> {
    return this.http.delete<any>('api/organisation/deleteBulks');
  }

  startUpload(): Observable<any> {
    return this.http.get<any>('api/organisation/startUpload');
  }

  endUpload(): Observable<any> {
    return this.http.get<any>('api/organisation/endUpload');
  }

  uploadCsv(fileToUpload: FileUpload): Observable<any> {
    return this.http.post<any>('api/organisation/upload', fileToUpload);
  }

  saveMappings(limit: number): Observable<any> {
    let params = new HttpParams();
    if (limit) params = params.append('limit', limit.toString());
    return this.http.get<any>('api/organisation/saveMappings', {params});
  }

  /*getStatistics(type: string): Observable<OrganisationManagerStatistics[]> {
    let params = new HttpParams();
    if (type) params = params.append('type', type);
    return this.http.get('api/organisation/statistics', {params});
  }*/

  getOrganisationTypes(): Observable<OrganisationType[]> {
    return this.http.get<OrganisationType[]>('api/organisation/organisationTypes');
  }

  getMultipleOrganisationsFromODSList(odsCodes: string[]):  Observable<Organisation[]> {
    let params = new HttpParams();
    for (let ix in odsCodes) {
      params = params.append('odsCodes', odsCodes[ix]);
    }
    return this.http.get<Organisation[]>('api/organisation/getMultipleOrganisationsFromODSList', {params});
  }

  searchOrganisationsInParentRegion(regionUUID: string, searchTerm: string):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (regionUUID) params = params.append('regionUUID', regionUUID);
    if (searchTerm) params = params.append('searchTerm', searchTerm);
    return this.http.get<Organisation[]>('api/organisation/searchOrganisationsInParentRegion', {params});
  }

  searchPublishersInDSA(dsaUuid: string, searchTerm: string):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (dsaUuid) params = params.append('dsaUUID', dsaUuid);
    if (searchTerm) params = params.append('searchTerm', searchTerm);
    return this.http.get<Organisation[]>('api/organisation/searchPublishersFromDSA', {params});
  }

  searchSubscribersInDSA(dsaUuid: string, searchTerm: string):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (dsaUuid) params = params.append('dsaUUID', dsaUuid);
    if (searchTerm) params = params.append('searchTerm', searchTerm);
    return this.http.get<Organisation[]>('api/organisation/searchSubscribersFromDSA', {params});
  }

  searchOrganisationsInParentRegionWithOdsList(regionUuid: string, odsCodes: string[]):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (regionUuid) params = params.append('regionUUID', regionUuid);
    for (let ix in odsCodes) {
      params.append('odsCodes', odsCodes[ix]);
    }
    return this.http.get<Organisation[]>('api/organisation/searchOrganisationsInParentRegionWithOdsList', {params});
  }

  searchPublishersFromDSAWithOdsList(dsaUuid: string, odsCodes: string[]):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (dsaUuid) params = params.append('dsaUUID', dsaUuid);
    for (let ix in odsCodes) {
      params.append('odsCodes', odsCodes[ix]);
    }
    return this.http.get<Organisation[]>('api/organisation/searchPublishersFromDSAWithOdsList', {params});
  }

  searchSubscribersFromDSAWithOdsList(dsaUuid: string, odsCodes: string[]):  Observable<Organisation[]> {
    let params = new HttpParams();
    if (dsaUuid) params = params.append('dsaUUID', dsaUuid);
    for (let ix in odsCodes) {
      params.append('odsCodes', odsCodes[ix]);
    }
    return this.http.get<Organisation[]>('api/organisation/searchSubscribersFromDSAWithOdsList', {params});
  }

}
