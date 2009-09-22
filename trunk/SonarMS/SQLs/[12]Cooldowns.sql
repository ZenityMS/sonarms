/*CoolDowns*/
DROP TABLE IF EXISTS `CoolDowns`;
CREATE TABLE `CoolDowns` (
  `id` int(11) NOT NULL auto_increment,
  `charid` int(11) NOT NULL,
  `SkillID` int(11) NOT NULL,
  `length` bigint(20) unsigned NOT NULL,
  `StartTime` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;