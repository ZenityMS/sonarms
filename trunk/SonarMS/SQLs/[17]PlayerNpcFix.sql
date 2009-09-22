ALTER TABLE `odinms`.`playernpcs` MODIFY COLUMN `gender` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `odinms`.`playernpcs` MODIFY COLUMN `dir` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `odinms`.`playernpcs_equip` ADD COLUMN `equippos` INT(11) UNSIGNED NOT NULL AFTER `type`;
