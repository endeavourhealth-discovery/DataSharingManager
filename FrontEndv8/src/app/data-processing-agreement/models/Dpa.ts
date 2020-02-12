import {Documentation} from "../../documentation/models/Documentation";
import { Purpose } from "src/app/models/Purpose";

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
          {label: 'Name', property: 'name', secondary: false},
          {label: 'Description', property: 'description', secondary: true}
      ];
  }
}
