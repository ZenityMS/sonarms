DROP TABLE IF EXISTS `odinms`.`user_entered`;
CREATE TABLE  `odinms`.`user_entered` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL,
  `script` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_user_entered_1` (`characterid`),
  CONSTRAINT `FK_user_entered_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;