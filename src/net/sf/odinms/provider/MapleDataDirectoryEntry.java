

/*
 * MapleDataDirectoryEntry.java
 *
 * Created on 26. November 2007, 22:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.provider;

import java.util.List;

/**
 *
 * @author Matze
 */
public interface MapleDataDirectoryEntry extends MapleDataEntry {
	public List<MapleDataDirectoryEntry> getSubdirectories();
	public List<MapleDataFileEntry> getFiles();
	public MapleDataEntry getEntry(String name);
}
