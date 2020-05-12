-- DSMDB_Patch6
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch10') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL IncreaseColumnSizeForODSCode();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch10', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE IncreaseColumnSizeForODSCode()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)

    ALTER TABLE data_sharing_manager.organisation
    MODIFY COLUMN ods_code varchar(20) DEFAULT NULL COMMENT 'ODS Code';

END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE IncreaseColumnSizeForODSCode;