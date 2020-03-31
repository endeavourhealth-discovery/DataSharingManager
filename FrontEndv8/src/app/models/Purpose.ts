export class Purpose {
    uuid: string;
    title: string;
    detail: string;

    getDisplayItems() :any[] {
        return [
            {label: 'Title', property: 'title'},
            {label: 'Detail', property: 'detail'},
        ];
    }
}
