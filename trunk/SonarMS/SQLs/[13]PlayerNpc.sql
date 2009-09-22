/*Player NPCs*/
DROP TABLE IF EXISTS `odinms`.`playernpcs`;
CREATE TABLE  `odinms`.`playernpcs` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(13) NOT NULL,
  `hair` int(11) NOT NULL,
  `face` int(11) NOT NULL,
  `skin` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `cy` int(11) NOT NULL default '0',
  `map` int(11) NOT NULL,
  `ScriptId` int(10) unsigned NOT NULL default '0',
  `Foothold` int(11) NOT NULL default '0',
  `rx0` int(11) NOT NULL default '0',
  `rx1` int(11) NOT NULL default '0',
  PRIMARY KEY  USING BTREE (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `odinms`.`playernpcs_equip`;
CREATE TABLE  `odinms`.`playernpcs_equip` (
  `id` int(11) NOT NULL auto_increment,
  `NpcId` int(11) NOT NULL default '0',
  `equipid` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

ALTER TABLE `playernpcs` ADD COLUMN `gender` INTEGER NOT NULL AFTER `map`;
ALTER TABLE `playernpcs` ADD COLUMN `dir` INTEGER NOT NULL AFTER `gender`;    
