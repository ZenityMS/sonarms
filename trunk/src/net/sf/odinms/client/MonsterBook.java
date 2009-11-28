/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.sf.odinms.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;

public class MonsterBook {

    private int SpecialCard = 0, NormalCard = 0, BookLevel = 1;
    private Map<Integer, Integer> cards = new LinkedHashMap<Integer, Integer>();

    public Map<Integer, Integer> getCards() {
    	return cards;
    }

    public int getSpecialCard() {
    	return SpecialCard;
    }

    public int getNormalCard() {
    	return NormalCard;
    }

    public int getBookLevel() {
    	return BookLevel;
    }
    
    public int getTotalCards() {
    	return SpecialCard + NormalCard;
    }

    public int getCardMobID(int id) {
        return MapleItemInformationProvider.getInstance().getCardMobId(id);
    }

    public void loadCards(final int charid) throws SQLException {
		Connection con = DatabaseConnection.getConnection();
	
		PreparedStatement ps = con.prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC");
		ps.setInt(1, charid);
		ResultSet rs = ps.executeQuery();
	
		int cardid, level;
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	
		while (rs.next()) {
		    cardid = rs.getInt("cardid");
		    level = rs.getInt("level");
	
		    if (ii.isSpecialCard(cardid)) {
		    	SpecialCard += level;
		    } else {
		    	NormalCard += level;
		    }
		    cards.put(cardid, level);
		}
		rs.close();
		ps.close();
	
		calculateLevel();
    }

    public void saveCards(final int charid) throws SQLException {
		if (cards.size() == 0) {
		    return;
		}
	
		Connection con = DatabaseConnection.getConnection();
	
		PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
		ps.setInt(1, charid);
		ps.execute();
		ps.close();
	
		boolean first = true;
		StringBuilder query = new StringBuilder();
	
		for (Entry<Integer, Integer> all : cards.entrySet()) {
		    if (first) {
			query.append("INSERT INTO monsterbook VALUES (");
			first = false;
		    } else {
			query.append(",(");
		    }
		    query.append(charid);
		    query.append(", ");
		    query.append(all.getKey()); // Card ID
		    query.append(", ");
		    query.append(all.getValue()); // Card level
		    query.append(")");
		}
		ps = con.prepareStatement(query.toString());
		ps.execute();
		ps.close();
    }

    private void calculateLevel() {
	int size = NormalCard + SpecialCard;
	BookLevel = 8;

	for (int i = 1; i <= 8; i++) {
	    if (size <= GameConstants.getBookLevel(i - 1)) {
		BookLevel = i;
		break;
	    }
	}
    }

    public void updateCard(final MapleClient c, final int cardid) {
        c.getSession().write(MaplePacketCreator.changeCover(cardid));
    }

    public void addCard(final MapleClient c, final int cardid) {
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showForeginCardEffect(c.getPlayer().getId()), false);
	
		for (Entry<Integer, Integer> all : cards.entrySet()) {
		    if (all.getKey() == cardid) {
				if (all.getValue() >= 5) {
				    c.getSession().write(MaplePacketCreator.addCard(true, cardid, all.getValue()));
				} else {
				    c.getSession().write(MaplePacketCreator.addCard(false, cardid, all.getValue()));
				    c.getSession().write(MaplePacketCreator.showGainCard());
				    all.setValue(+1);
				    calculateLevel();
				}
				return;
		    }
        }
	
        // New card
		cards.put(cardid, 1);
		c.getSession().write(MaplePacketCreator.addCard(false, cardid, 1));
		c.getSession().write(MaplePacketCreator.showGainCard());
		calculateLevel();
    }
}
