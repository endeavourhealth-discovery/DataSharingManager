-- DSMDB_Patch1
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch1') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL AddColumnToDataset();
        CALL AddColumnToCohort();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch1', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AddColumnToDataset()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='technical_definition'
    AND TABLE_NAME='dataset' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.dataset
        ADD COLUMN technical_definition mediumtext null COMMENT 'the technical definition of the cohort';

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AddColumnToCohort()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='technical_definition'
    AND TABLE_NAME='cohort' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.cohort
        ADD COLUMN technical_definition mediumtext null COMMENT 'the technical definition of the cohort';

    END IF;

END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE AddColumnToDataset;
DROP PROCEDURE AddColumnToCohort;