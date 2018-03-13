export class DataSet {
	uuid: string;
	name: string;
	description: string;
	dpas: { [key: string]: string; };

	getDisplayItems(): any[] {
		return [
			{label: 'Description', property: 'description'}
		];
	}
}
