

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.odinms.net.world.remote;

import java.io.Serializable;

/**
 *
 * @author Matze
 */
public class WorldLocation implements Serializable {
	private static final long serialVersionUID = 2226165329466413678L;
	
	public int map;
	public int channel;

	public WorldLocation(int map, int channel) {
		this.map = map;
		this.channel = channel;
	}
	
}
