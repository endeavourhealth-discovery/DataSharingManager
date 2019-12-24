export class Cohort {
    uuid : string;
    name:string;
    consentModelId: string;
    description: string;
    technicalDefinition: string;
    dpas : { [key:string]:string; };

    getDisplayItems() :any[] {
        return [
            {label: 'name', property: 'name'}
        ];
    }
}
