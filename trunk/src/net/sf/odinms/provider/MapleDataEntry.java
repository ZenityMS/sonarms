

/*
 * MapleDataEntry.java
 *
 * Created on 26. November 2007, 22:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.provider;

/**
 *
 * @author Matze
 */
public interface MapleDataEntry extends MapleDataEntity {
	public String getName();
	public int getSize();
	public int getChecksum();
	public int getOffset();
}
