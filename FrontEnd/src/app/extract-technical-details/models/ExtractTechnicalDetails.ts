export class ExtractTechnicalDetails {
  uuid: string;
  name: string;
  sftpHostName: string;
  sftpHostDirectory: string;
  sftpHostPort: number;
  sftpClientUsername: string;
  sftpClientPrivateKeyPassword: string;

  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'extractTechnicalDetailsName'},
      {label: 'SFTP host name', property: 'sftpHostName'},
      {label: 'SFTP host directory', property: 'sftpHostDirectory'},
      {label: 'SFTP host port', property: 'sftpHostPort'},
      {label: 'SFTP client username', property: 'sftpClientUsername'},
      {label: 'SFTP client private key password', property: 'sftpClientPrivateKeyPassword'},
    ];
  }

}
