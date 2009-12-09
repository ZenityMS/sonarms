/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.channel.handler;

import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleWeaponType;
import client.SkillFactory;
import constants.InventoryConstants;
import tools.Randomizer;
import constants.SkillConstants;
import net.MaplePacket;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class RangedAttackHandler extends AbstractDealDamageHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        AttackInfo attack = parseDamage(slea, true);
        MapleCharacter player = c.getPlayer();
        if (attack.skill == 5121002) {
            player.getMap().broadcastMessage(player, MaplePacketCreator.rangedAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, 0, attack.allDamage, attack.speed), false);
            applyAttack(attack, player, 9999999, 1);
        } else {
            MapleInventory equip = player.getInventory(MapleInventoryType.EQUIPPED);
            IItem weapon = equip.getItem((byte) -11);
            MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
            MapleWeaponType type = mii.getWeaponType(weapon.getItemId());
            if (type == MapleWeaponType.NOT_A_WEAPON)
                throw new RuntimeException("[h4x] Player " + player.getName() + " is attacking with something that's not a weapon");
            MapleInventory use = player.getInventory(MapleInventoryType.USE);
            int projectile = 0;
            int bulletCount = 1;
            MapleStatEffect effect = null;
            if (attack.skill != 0) {
                effect = attack.getAttackEffect(c.getPlayer());
                bulletCount = effect.getBulletCount();
                if (effect.getCooldown() > 0)
                    c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
            }
            boolean hasShadowPartner = player.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null;
            int damageBulletCount = bulletCount;
            if (hasShadowPartner)
                bulletCount *= 2;
            for (int i = 0; i < 255; i++) { // not sure why 255, shouldn't it be 100?
                IItem item = use.getItem((byte) i);
                if (item != null) {
                    boolean clawCondition = type == MapleWeaponType.CLAW && InventoryConstants.isThrowingStar(item.getItemId()) && weapon.getItemId() != 1472063;
                    boolean bowCondition = type == MapleWeaponType.BOW && InventoryConstants.isArrowForBow(item.getItemId());
                    boolean crossbowCondition = type == MapleWeaponType.CROSSBOW && InventoryConstants.isArrowForCrossBow(item.getItemId());
                    boolean gunCondition = type == MapleWeaponType.GUN && InventoryConstants.isBullet(item.getItemId());
                    boolean mittenCondition = weapon.getItemId() == 1472063 && (InventoryConstants.isArrowForBow(item.getItemId()) || InventoryConstants.isArrowForCrossBow(item.getItemId()));
                    if ((clawCondition || bowCondition || crossbowCondition || mittenCondition || gunCondition) && item.getQuantity() >= bulletCount) {
                        projectile = item.getItemId();
                        break;
                    }
                }
            }
            boolean soulArrow = player.getBuffedValue(MapleBuffStat.SOULARROW) != null;
            boolean shadowClaw = player.getBuffedValue(MapleBuffStat.SHADOW_CLAW) != null;
            if (!soulArrow && !shadowClaw && attack.skill != 11101004 && attack.skill != 15111007 && attack.skill != 14101006) {
                int bulletConsume = bulletCount;
                if (effect != null && effect.getBulletConsume() != 0)
                    bulletConsume = effect.getBulletConsume() * (hasShadowPartner ? 2 : 1);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false, true);
            }
            if (projectile != 0 || soulArrow || attack.skill == 11101004 || attack.skill == 15111007 || attack.skill == 14101006) {
                int visProjectile = projectile; //visible projectile sent to players
                if (InventoryConstants.isThrowingStar(projectile)) {
                    MapleInventory cash = player.getInventory(MapleInventoryType.CASH);
                    for (int i = 0; i < 255; i++) { // impose order...
                        IItem item = cash.getItem((byte) i);
                        if (item != null)
                            if (item.getItemId() / 1000 == 5021) {
                                visProjectile = item.getItemId();
                                break;
                            }
                    }
                } else //bow, crossbow
                if (soulArrow || attack.skill == 3111004 || attack.skill == 3211004 || attack.skill == 11101004 || attack.skill == 15111007 || attack.skill == 14101006)
                    visProjectile = 0;
                MaplePacket packet;
                try {
                    switch (attack.skill) {
                        case 3121004: // Hurricane
                        case 3221001: // Pierce
                        case 5221004: // Rapid Fire
                        case 13111002: // KoC Hurricane
                            packet = MaplePacketCreator.rangedAttack(player.getId(), attack.skill, attack.direction, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed);
                            break;
                        default:
                            packet = MaplePacketCreator.rangedAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed);
                            break;
                    }
                    player.getMap().broadcastMessage(player, packet, false, true);
                } catch (Exception e) {
                }
                int basedamage;
                int projectileWatk = 0;
                if (projectile != 0)
                    projectileWatk = mii.getWatkForProjectile(projectile);
                if (attack.skill != 4001344 && attack.skill != 14001004) // not lucky 7
                    if (projectileWatk != 0)
                        basedamage = c.getPlayer().calculateMaxBaseDamage(c.getPlayer().getTotalWatk() + projectileWatk);
                    else
                        basedamage = c.getPlayer().getCurrentMaxBaseDamage();
                else // l7 has a different formula :>
                    basedamage = (int) (((c.getPlayer().getTotalLuk() * 5.0) / 100.0) * (c.getPlayer().getTotalWatk() + projectileWatk));
                if (attack.skill == 3101005) //arrowbomb is hardcore like that �.o
                    basedamage *= effect.getX() / 100.0;
                int maxdamage = basedamage;
                double critdamagerate = 0.0;
                if (player.getJob().isA(MapleJob.ASSASSIN) || player.getJob().isA(MapleJob.NIGHTWALKER2)) {
                    ISkill criticalthrow = player.isCygnus() ? SkillFactory.getSkill(14100001) : SkillFactory.getSkill(4100001);
                    int critlevel = player.getSkillLevel(criticalthrow);
                    if (critlevel > 0)
                        critdamagerate = (criticalthrow.getEffect(critlevel).getDamage() / 100.0);
                } else if (player.getJob().isA(MapleJob.BOWMAN) || player.getJob().isA(MapleJob.WINDARCHER1)) {
                    ISkill criticalshot = player.isCygnus() ? SkillFactory.getSkill(13000000) : SkillFactory.getSkill(3000001);
                    int critlevel = player.getSkillLevel(criticalshot);
                    if (critlevel > 0)
                        critdamagerate = (criticalshot.getEffect(critlevel).getDamage() / 100.0) - 1.0;
                }
                int critdamage = (int) (basedamage * critdamagerate);
                if (effect != null)
                    maxdamage *= attack.skill == 14101006 ? effect.getDamage() : effect.getDamage() / 100.0;
                maxdamage += critdamage;
                maxdamage *= damageBulletCount;
                if (hasShadowPartner) {
                    int id = player.isCygnus() ? 14111000 : 4111002;
                    ISkill shadowPartner = SkillFactory.getSkill(id);
                    int shadowPartnerLevel = player.getSkillLevel(shadowPartner);
                    MapleStatEffect shadowPartnerEffect = shadowPartner.getEffect(shadowPartnerLevel);
                    if (attack.skill != 0)
                        maxdamage *= (1.0 + shadowPartnerEffect.getY() / 100.0);
                    else
                        maxdamage *= (1.0 + shadowPartnerEffect.getX() / 100.0);
                }
                if (attack.skill == 4111004)
                    maxdamage = 35000;
                if (effect != null) {
                    int money = effect.getMoneyCon();
                    if (money != 0) {
                        int moneyMod = money / 2;
                        money += Randomizer.getInstance().nextInt(moneyMod);
                        if (money > player.getMeso())
                            money = player.getMeso();
                        player.gainMeso(-money, false);
                    }
                }
                if (attack.skill != 0) {
                    ISkill skill = SkillFactory.getSkill(attack.skill);
                    int skillLevel = c.getPlayer().getSkillLevel(skill);
                    MapleStatEffect effect_ = skill.getEffect(skillLevel);
                    if (effect_.getCooldown() > 0)
                        if (player.skillisCooling(attack.skill))
                            return;
                        else {
                            c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
                            ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), attack.skill), effect_.getCooldown() * 1000);
                            player.addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000, timer);
                        }
                }
                if ((player.getSkillLevel(SkillFactory.getSkill(14100005)) > 0 || player.getSkillLevel(SkillFactory.getSkill(SkillConstants.WindArcher.WindWalk)) > 0) && player.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && attack.numAttacked > 0 && player.getBuffSource(MapleBuffStat.DARKSIGHT) != 9101004) {
                    player.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
                    player.cancelBuffStats(MapleBuffStat.DARKSIGHT);
                }
                applyAttack(attack, player, maxdamage, bulletCount);
            }
        }
    }
}
