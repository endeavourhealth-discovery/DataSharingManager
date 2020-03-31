import {Documentation} from "../../documentation/models/Documentation";
import {Purpose} from "../../models/Purpose";

export class Dsa {
    uuid: string;
    name: string;
    description: string;
    derivation: string;
    dsaStatusId: number;
    consentModelId: number;
    startDate: string;
    endDate: string;
    dataFlows: { [key: string]: string; };
    regions: { [key: string]: string; };
    publishers: { [key: string]: string; };
    subscribers: { [key: string]: string; };
    projects: { [key: string]: string; };
    cohorts: { [key: string]: string; };
    dataSets: { [key: string]: string; };
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
