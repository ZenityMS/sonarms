/*Marriages*/
ALTER TABLE `characters`
ADD COLUMN `married` INTEGER UNSIGNED NOT NULL DEFAULT 0,
ADD COLUMN `partnerid` INTEGER UNSIGNED NOT NULL DEFAULT 0,
ADD COLUMN `cantalk` INTEGER UNSIGNED NOT NULL DEFAULT 1,
ADD COLUMN `marriagequest` INTEGER UNSIGNED NOT NULL DEFAULT 0;

CREATE TABLE `marriages` (
  `marriageid` INTEGER UNSIGNED NOT NULL DEFAULT NULL AUTO_INCREMENT,
  `husbandid` INTEGER UNSIGNED NOT NULL DEFAULT 0,
  `wifeid` INTEGER UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`marriageid`)
)
ENGINE = InnoDB;