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
  pgpCustomerPublicKeyFilename: string
  pgpCustomerPublicKeyFileData: string;
  pgpInternalPublicKeyFilename: string;
  pgpInternalPublicKeyFileData: string;

  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'name'},
      {label: 'SFTP host name', property: 'sftpHostName'},
      {label: 'SFTP client username', property: 'sftpClientUsername'},
    ];
  }

}
