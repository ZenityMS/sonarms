

/*
 * InventoryException.java
 *
 * Created on 26. November 2007, 15:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.client;

/**
 * 
 * @author Matze
 */
public class InventoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/** Creates a new instance of InventoryException */
	public InventoryException() {
		super();
	}

	public InventoryException(String msg) {
		super(msg);
	}
}
