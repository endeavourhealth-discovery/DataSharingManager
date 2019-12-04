-- DSMDB_Patch3
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch3') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL CreateProjectSchedulesTable();
        CALL AddScheduleMapType();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch3', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE CreateProjectSchedulesTable()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
	CREATE TABLE `project_schedule` (
	  `uuid` char(36) NOT NULL COMMENT 'Unique identifier for the schedule',
	  `cron_expression` varchar(200) DEFAULT NULL,
	  `cron_description` varchar(250) DEFAULT NULL,
	  `cron_settings` varchar(100) DEFAULT NULL,
	  PRIMARY KEY (`uuid`)
	) COMMENT='Information regarding project schedule';

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AddScheduleMapType()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    IF ((SELECT map_type FROM data_sharing_manager.map_type WHERE id = 16) IS NULL)

    THEN
        INSERT INTO data_sharing_manager.map_type (id, map_type)
        VALUES (16, "Schedule");

    END IF;

END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE CreateProjectSchedulesTable;
DROP PROCEDURE AddScheduleMapType;