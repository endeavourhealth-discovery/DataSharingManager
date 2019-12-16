-- DSMDB_Patch2
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch2') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL CreateExtractTechnicalDetailsTable();
        CALL AddExtractTechnicalDetailsMapType();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch2', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE CreateExtractTechnicalDetailsTable()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    CREATE TABLE IF NOT EXISTS data_sharing_manager.extract_technical_details (
        uuid char(36) NOT NULL COMMENT 'Unique identifier for the extract technical details',
        `name` varchar(200) COMMENT 'Name of the extract technical details',
        sftp_host_name varchar(100) COMMENT 'SFTP host name',
        sftp_host_directory varchar(100) COMMENT 'SFTP host directory, typically /ftp/',
        sftp_host_port varchar(10) COMMENT 'SFTP host port',
        sftp_client_username varchar(100) COMMENT 'SFTP client username',
        sftp_client_private_key_password varchar(100) COMMENT 'SFTP client private key password, rarely used',
        sftp_host_public_key_filename varchar(100) COMMENT 'Filename for the SFTP host public key, rarely used (.pub)',
        sftp_host_public_key_fileData mediumtext COMMENT 'Base64 encoded file data for the SFTP host public key, rarely used (.pub)',
        sftp_client_private_key_filename varchar(100) COMMENT 'Filename for the SFTP client private key (.ppk)',
        sftp_client_private_key_fileData mediumtext COMMENT 'Base64 encoded file data for the SFTP client private key (.ppk)',
        pgp_customer_public_key_filename varchar(100) COMMENT 'Filename for the PGP customer public key (.cer)',
        pgp_customer_public_key_fileData mediumtext COMMENT 'Base64 encoded file data for the PGP customer public key (.cer)',
        pgp_internal_public_key_filename varchar(100) COMMENT 'Filename for the PGP internal public key (.cer)',
        pgp_internal_public_key_fileData mediumtext COMMENT 'Base64 encoded file data for the PGP internal public key (.cer)',

        PRIMARY KEY data_sharing_manager_extract_technical_details_uuid (uuid)
    ) COMMENT 'Hold extract technical details';

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AddExtractTechnicalDetailsMapType()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    IF ((SELECT map_type FROM data_sharing_manager.map_type WHERE id = 15) IS NULL)

    THEN
        INSERT INTO data_sharing_manager.map_type (id, map_type)
        VALUES (15, "Extract Technical Details");

    END IF;

END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE CreateExtractTechnicalDetailsTable;
DROP PROCEDURE AddExtractTechnicalDetailsMapType;