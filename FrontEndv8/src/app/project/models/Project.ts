import {Documentation} from "../../documentation/models/Documentation";
import {ExtractTechnicalDetails} from "./ExtractTechnicalDetails";
import {Schedule} from "../../scheduler/models/Schedule";

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
  outputFormat: number;
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
  schedules: { [key: string]: string; };

  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'name'}
    ];
  }
}
