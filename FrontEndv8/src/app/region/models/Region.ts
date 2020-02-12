export class Region {
    uuid: string;
    name: string;
    description: string;
    organisationCount: number;
    organisations: { [key: string]: string; };
    parentRegions: { [key: string]: string; };
    childRegions: { [key: string]: string; };
    sharingAgreements: { [key: string]: string; };
    processingAgreements: { [key: string]: string; };

    getDisplayItems(): any[] {
        return [
            {label: 'Name', property: 'name', secondary: false},
            {label: 'Description', property: 'description', secondary: true},
        ];
    }
}
