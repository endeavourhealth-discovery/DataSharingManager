insert into data_sharing_manager.map_type (id, map_type)
values (15, "Extract Technical Details");

drop table if exists data_sharing_manager.extract_technical_details;

create table `extract_technical_details` (

    uuid char(36) NOT NULL COMMENT 'Unique identifier for the extract technical details',
    name varchar(200) NOT NULL COMMENT 'Name of the extract technical details',
    sftp_host_name varchar(200) NOT NULL COMMENT 'SFTP host name',

    -- for now, just a subset of all the required fields, while doing development

    -- `sftp_host_public_key` mediumtext COMMENT 'Base64 encoded file data for the SFTP host public key, rarely used (.pub)',
    -- `sftp_host_directory` varchar(200) NOT NULL COMMENT 'SFTP host directory, typically /ftp/',
    -- `sftp_host_port` int(3) NOT NULL COMMENT 'SFTP host port',
    -- `sftp_client_username` varchar(200) NOT NULL COMMENT 'SFTP client username',
    -- `sftp_client_private_key_password` varchar(200) COMMENT 'SFTP client private key password, rarely used',
    -- `sftp_client_private_key` mediumtext NOT NULL COMMENT 'Base64 encoded file data for the SFTP client private key (.ppk)',
    -- `pgp_customer_public_key` mediumtext NOT NULL COMMENT 'Base64 encoded file data for the customer pgp public key (.cer)',
    -- `pgp_archive_public_key` mediumtext NOT NULL COMMENT 'Base64 encoded file data for the archive pgp public key (.cer)',
    -- `pgp_archive_private_key` mediumtext NOT NULL COMMENT 'Base64 encoded file data for the archive pgp private key (.p12)',

    primary key data_sharing_manager_extract_technical_details_uuid (uuid)
) comment 'Hold extract technical details';
