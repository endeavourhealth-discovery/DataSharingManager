import {Purpose} from './Purpose';
import {Documentation} from "./Documentation";

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
    documentations: Documentation[];
    purposes: Purpose[];
    benefits: Purpose[];

    getDisplayItems(): any[] {
        return [
            {label: 'Description', property: 'description'},
            {label: 'Status', property: 'dsaStatusId'}
        ];
    }
}
