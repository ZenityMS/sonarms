ALTER TABLE `accounts` DROP COLUMN `email`,
 DROP COLUMN `emailcode`,
 DROP COLUMN `forumaccid`,
 DROP COLUMN `lastknownip`,
 DROP COLUMN `lastpwemail`,
 DROP COLUMN `tempban`,
 DROP COLUMN `greason`,
 DROP COLUMN `password2`;
DROP TABLE cashshop;
ALTER TABLE `characters` DROP COLUMN `petid`,
 DROP COLUMN `married`,
 DROP COLUMN `partnerid`,
 DROP COLUMN `cantalk`,
 DROP COLUMN `reborns`,
 DROP COLUMN `marriagequest`;
DROP TABLE cheatlog;
DROP TABLE engagements;
DROP TABLE inventorylog;
ALTER TABLE `accounts` ADD COLUMN `pin` VARCHAR(10) NOT NULL AFTER `password`;
ALTER TABLE `accounts` MODIFY COLUMN `pin` VARCHAR(10) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;



ALTER TABLE `playernpcs_equip` MODIFY COLUMN `type` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `playernpcs_equip` MODIFY COLUMN `equippos` INT(11) NOT NULL;

ALTER TABLE `characters` ADD COLUMN `vanquisherStage` INT(11) UNSIGNED NOT NULL DEFAULT 0 AFTER `allianceRank`, ADD COLUMN `dojoPoints` INT(11) UNSIGNED NOT NULL DEFAULT 0 AFTER `vanquisherStage`;
ALTER TABLE `characters` ADD COLUMN `lastDojoStage` INT(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `dojoPoints`;
ALTER TABLE `characters` ADD COLUMN `finishedDojoTutorial` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 AFTER `lastDojoStage`;
ALTER TABLE `characters` ADD COLUMN `vanquisherKills` INT(11) UNSIGNED NOT NULL DEFAULT 0 AFTER `finishedDojoTutorial`;

ALTER TABLE `savedlocations` MODIFY COLUMN `locationtype` ENUM('FREE_MARKET','WORLDTOUR','FLORINA','DOJO','CYGNUSINTRO') NOT NULL;
ALTER TABLE `inventoryitems` ADD COLUMN `expiredate` INT(20) NOT NULL DEFAULT '-1' AFTER `petid`;