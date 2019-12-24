import {Documentation} from "../../documentation/models/Documentation";

export class DataFlow {
    uuid: string;
    name: string;
    purpose: string;
    storageProtocolId: number;
    deidentificationLevel: number;
    consentModelId: number;
    dsas: { [key: string]: string; };
    dpas: { [key: string]: string; };
    exchanges: { [key: string]: string; };
    publishers: { [key: string]: string; };
    subscribers: { [key: string]: string; };
    documentations: Documentation[];

    getDisplayItems(): any[] {
        return [
            {label: 'Purpose', property: 'purpose'}
        ];
    }
}
