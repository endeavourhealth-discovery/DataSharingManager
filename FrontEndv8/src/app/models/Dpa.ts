import {Documentation} from "./Documentation";
import {Purpose} from "./Purpose";

export class Dpa {
  uuid: string;
  name: string;
  description: string;
  derivation: string;
  publisherInformation: string;
  publisherContractInformation: string;
  publisherDataset: string;
  dsaStatusId: number;
  returnToSenderPolicy: string;
  startDate: string;
  endDate: string;
  dataFlows: { [key: string]: string; };
  regions: { [key: string]: string; };
  cohorts: { [key: string]: string; };
  dataSets: { [key: string]: string; };
  publishers: { [key: string]: string; };
  documentations: Documentation[];
  purposes: Purpose[];
  benefits: Purpose[];

  getDisplayItems(): any[] {
      return [
          {label: 'Description', property: 'description'},
          {label: 'Derivation', property: 'derivation'},
          {label: 'Publisher information', property: 'publisherInformation'},
          {label: 'Publisher contract information', property: 'publisherContractInformation'},
          {label: 'Publisher data set', property: 'publisherDataSet'},
          {label: 'DSA status id', property: 'dsaStatusId'},
          {label: 'Return to sender policy', property: 'returnToSenderPolicy'}
      ];
  }
}
