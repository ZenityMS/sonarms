/*Hired Merchants*/
DROP TABLE IF EXISTS `odinms`.`hiredmerchant`;
CREATE TABLE  `odinms`.`hiredmerchant` (
  `id` int(11) NOT NULL auto_increment,
  `ownerid` int(11) default '0',
  `itemid` int(10) unsigned NOT NULL default '0',
  `quantity` int(10) unsigned NOT NULL default '0',
  `upgradeslots` int(11) default '0',
  `level` int(11) default '0',
  `str` int(11) default '0',
  `dex` int(11) default '0',
  `int` int(11) default '0',
  `luk` int(11) default '0',
  `hp` int(11) default '0',
  `mp` int(11) default '0',
  `watk` int(11) default '0',
  `matk` int(11) default '0',
  `wdef` int(11) default '0',
  `mdef` int(11) default '0',
  `acc` int(11) default '0',
  `avoid` int(11) default '0',
  `hands` int(11) default '0',
  `speed` int(11) default '0',
  `jump` int(11) default '0',
  `owner` varchar(13) default '',
  `type` tinyint(1) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

ALTER TABLE `characters` ADD COLUMN `MerchantMesos` int(11) default '0';
ALTER TABLE `characters` ADD COLUMN `HasMerchant` tinyint(1) default '0';