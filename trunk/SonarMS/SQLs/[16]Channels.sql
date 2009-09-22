INSERT INTO `channels` (`channelid`,`world`,`number`,`key`) VALUES
 (5,0,5,'113f78f519e010e65853241bfcb14450c4fccb66'),
 (6,0,6,'4abba5486022346a2b309c1c2ea6a0da41a88090'),
 (7,0,7,'76134d11fe0c2b337e2b786bfcc738b975fcf40a'),
 (8,0,8,'5688c244c56a884a50984130a17d0b61d06743a3'),
 (9,0,9,'6e59a6559033c70b98148f1bd67e1b63aaeedf30'),
 (10,0,10,'603dd499e4b134bf9925600b7f150644f9e9a50b'),
 (11,0,11,'b48f4c3c803f58950b005d785cf828027a83eac4'),
 (12,0,12,'52a9458618abed6a42e228b33ade9cdf5ded10b4'),
 (13,0,13,'190535a9ffb4d4d688ac1f3fa7dc09a6c81c3b86'),
 (14,0,14,'5ce2b432ac85290b411ef0975b96712c1c35591a'),
 (15,0,15,'7d8bae4945561008426174be907142196ed84275'),
 (16,0,16,'da0517603d42ce6f9d9bdf4871bc1ecbf7a20c3c'),
 (17,0,17,'87c56d1e33cf26f48ac76f1bd76b6637cddd9548'),
 (18,0,18,'fbce35ee8db37d9bf02f444c65e49fb8a9685c28'),
 (19,0,19,'51a2bb10ecf4e2e28fe62b405106baadb0d11090'),
 (20,0,20,'9a071c700e4c051c354817f7e2482d148380d574');
/*!40000 ALTER TABLE `channels` ENABLE KEYS */;

DROP TABLE IF EXISTS `channelconfig`;
CREATE TABLE `channelconfig` (
  `channelconfigid` int(10) unsigned NOT NULL auto_increment,
  `channelid` int(10) unsigned NOT NULL default '0',
  `name` tinytext NOT NULL,
  `value` tinytext NOT NULL,
  PRIMARY KEY  (`channelconfigid`),
  KEY `channelid` (`channelid`),
  CONSTRAINT `channelconfig_ibfk_1` FOREIGN KEY (`channelid`) REFERENCES `channels` (`channelid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `channelconfig`
--

/*!40000 ALTER TABLE `channelconfig` DISABLE KEYS */;
INSERT INTO `channelconfig` (`channelconfigid`,`channelid`,`name`,`value`) VALUES 
 (1,2,'net.sf.odinms.channel.net.port','7576'),
 (2,3,'net.sf.odinms.channel.net.port','7577'),
 (3,4,'net.sf.odinms.channel.net.port','7578'),
 (4,5,'net.sf.odinms.channel.net.port','7589'),
 (5,6,'net.sf.odinms.channel.net.port','7590'),
 (6,7,'net.sf.odinms.channel.net.port','7591'),
 (7,8,'net.sf.odinms.channel.net.port','7592'),
 (8,9,'net.sf.odinms.channel.net.port','7593'),
 (9,10,'net.sf.odinms.channel.net.port','7594'),
 (10,11,'net.sf.odinms.channel.net.port','7595'),
 (11,12,'net.sf.odinms.channel.net.port','7596'),
 (12,13,'net.sf.odinms.channel.net.port','7597'),
 (13,14,'net.sf.odinms.channel.net.port','7598'),
 (14,15,'net.sf.odinms.channel.net.port','7599'),
 (15,16,'net.sf.odinms.channel.net.port','7600'),
 (16,17,'net.sf.odinms.channel.net.port','7601'),
 (17,18,'net.sf.odinms.channel.net.port','7602'),
 (18,19,'net.sf.odinms.channel.net.port','7603'),
 (19,20,'net.sf.odinms.channel.net.port','7604');
/*!40000 ALTER TABLE `channelconfig` ENABLE KEYS */;

update channelconfig set name = 'channel.net.port';