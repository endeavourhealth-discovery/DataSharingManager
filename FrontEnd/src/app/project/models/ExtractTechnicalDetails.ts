export class ExtractTechnicalDetails {
  uuid: string;
  name: string;
  sftpHostName: string;
  sftpHostDirectory: string;
  sftpHostPort: string;
  sftpClientUsername: string;
  sftpClientPrivateKeyPassword: string;
  sftpHostPublicKeyFilename: string;
  sftpHostPublicKeyFileData: string;
  sftpClientPrivateKeyFilename: string;
  sftpClientPrivateKeyFileData: string;

  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'extractTechnicalDetailsName'},
      {label: 'SFTP host name', property: 'sftpHostName'},
      {label: 'SFTP host directory', property: 'sftpHostDirectory'},
      {label: 'SFTP host port', property: 'sftpHostPort'},
      {label: 'SFTP client username', property: 'sftpClientUsername'},
      {label: 'SFTP client private key password', property: 'sftpClientPrivateKeyPassword'},
      {label: 'SFTP host public key filename', property: 'sftpHostPublicKeyFilename'},
      {label: 'SFTP host public key file data', property: 'sftpHostPublicKeyFileData'},
      {label: 'SFTP client private key filename', property: 'sftpClientPrivateKeyFilename'},
      {label: 'SFTP client private key file data', property: 'sftpClientPrivateKeyFileData'},
    ];
  }

}
