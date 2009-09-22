/*Slots*/
ALTER TABLE `odinms`.`characters` ADD COLUMN `equipslots` INTEGER NOT NULL DEFAULT 48 AFTER `HasMerchant`,
 ADD COLUMN `useslots` INTEGER NOT NULL DEFAULT 48 AFTER `equipslots`,
 ADD COLUMN `setupslots` INTEGER NOT NULL DEFAULT 48 AFTER `useslots`,
 ADD COLUMN `etcslots` INTEGER NOT NULL DEFAULT 48 AFTER `setupslots`;