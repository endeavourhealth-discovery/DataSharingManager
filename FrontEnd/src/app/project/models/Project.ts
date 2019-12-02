import {Documentation} from "../../documentation/models/Documentation";
import {ExtractTechnicalDetails} from "../../extract-technical-details/models/ExtractTechnicalDetails";
import {Schedule} from "./Schedule";

export class Project {
  uuid: string;
  name: string;
  leadUser: string;
  technicalLeadUser: string;
  consentModelId: number;
  deidentificationLevel: number;
  projectTypeId: number;
  securityInfrastructureId: number;
  ipAddress: string;
  summary: string;
  businessCase: string;
  objectives: string;
  securityArchitectureId: number;
  storageProtocolId: number;
  businessCaseStatus: number;
  flowScheduleId: number;
  projectStatusId: number;
  startDate: string;
  endDate: string;
  publishers: { [key: string]: string; };
  subscribers: { [key: string]: string; };
  cohorts: { [key: string]: string; };
  dataSets: { [key: string]: string; };
  projectConfiguration: { [key: string]: string; };
  dsas: { [key: string]: string; };
  applicationPolicy: string;
  documentations: Documentation[];
  extractTechnicalDetails: ExtractTechnicalDetails;
  schedule: Schedule;

  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'name'}
    ];
  }
}
