/*PQ/Drops Fix*/
update monsterdrops set chance = chance * 10 where monsterid >= 9420500 and monsterid <= 9420512;