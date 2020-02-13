-- DSMDB_Patch6
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch6') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL AddColumnsToExtraDetails();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch6', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AddColumnsToExtraDetails()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='output_format'
                                                           AND TABLE_NAME='extract_technical_details' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.extract_technical_details
        ADD COLUMN output_format SMALLINT(6) NULL COMMENT 'Output format';

    END IF;

    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='security_infrastructure'
                                                           AND TABLE_NAME='extract_technical_details' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.extract_technical_details
        ADD COLUMN security_infrastructure SMALLINT(6) NULL COMMENT 'Security infrastructure';

    END IF;

    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='security_architecture'
                                                           AND TABLE_NAME='extract_technical_details' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.extract_technical_details
        ADD COLUMN security_architecture SMALLINT(6) NULL COMMENT 'Security architecture';

    END IF;


END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE AddColumnsToExtraDetails;