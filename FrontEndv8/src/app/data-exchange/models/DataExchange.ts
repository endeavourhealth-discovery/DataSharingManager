import {Documentation} from "../../documentation/models/Documentation";

export class DataExchange {
    uuid: string;
    name: string;
    publisher: boolean;
    systemName: string;
    directionId: number;
    flowScheduleId: number;
    approximateVolume: number;
    dataExchangeMethodId: number;
    securityInfrastructureId: number;
    securityArchitectureId: number;
    flowStatusId: number;
    endpoint: string;
    dataFlows: { [key: string]: string; };

    getDisplayItems(): any[] {
        return [
            {label: 'Flow status', property: 'flowStatusId'},
            {label: 'Approximate volume', property: 'approximateVolume'},
            {label: 'System name', property: 'systemName'},
            {label: 'Endpoint', property: 'endpoint'},
            {label: 'Publisher or subscriber', property: 'publisher'}
        ];
    }
}
