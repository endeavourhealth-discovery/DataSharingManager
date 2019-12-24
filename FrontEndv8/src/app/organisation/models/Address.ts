export class Address {
    uuid : string;
    organisationUuid:string;
    buildingName: string;
    numberAndStreet: string;
    locality: string;
    city: string;
    county: string;
    postcode: string;
    lat: number;
    lng: number;
    geolocationReprocess: boolean;

  getDisplayItems(): any[] {
    return [
      {label: 'Building name', property: 'buildingName'},
      {label: 'Number and street', property: 'numberAndStreet'},
      {label: 'Locality', property: 'locality'},
      {label: 'City', property: 'city'},
      {label: 'County', property: 'county'},
      {label: 'Postcode', property: 'postcode'}
    ];
  }
}
