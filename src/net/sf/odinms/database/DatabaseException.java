

/*
 * DatabaseException.java
 *
 * Created on 28. November 2007, 13:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.database;

/**
 *
 * @author Matze
 */
public class DatabaseException extends RuntimeException {
	private static final long serialVersionUID = -420103154764822555L;

	/** Creates a new instance of DatabaseException */
	public DatabaseException() {
	}
	
	public DatabaseException(String msg) {
		super(msg);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
}
