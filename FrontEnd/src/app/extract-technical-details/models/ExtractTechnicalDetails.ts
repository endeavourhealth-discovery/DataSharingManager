export class ExtractTechnicalDetails {
  uuid: string;
  name: string;
  sftpHostName: string;
  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'extractTechnicalDetailsName'},
      {label: 'SFTP Host Name', property: 'sftpHostName'},
    ];
  }

}
