

package net.sf.odinms.tools;

/**
 * Provides a suite of tools for manipulating Korean Timestamps.
 * 
 * @author Frz
 * @since Revision 746
 * @version 1.0
 */
public class KoreanDateUtil {
	private final static int ITEM_YEAR2000 = -1085019342;
	private final static long REAL_YEAR2000 = 946681229830l;
	private final static int QUEST_UNIXAGE = 27111908;
	private final static long FT_UT_OFFSET = 116444736000000000L; // 100 nsseconds from 1/1/1601 -> 1/1/1970

	/**
	 * Dummy constructor for static classes.
	 */
	private KoreanDateUtil() {
	}

	/**
	 * Converts a Unix Timestamp into File Time
	 * 
	 * @param realTimestamp The actual timestamp in milliseconds.
	 * @return A 64-bit long giving a filetime timestamp
	 */
	public static long getTempBanTimestamp(long realTimestamp) {
		// long time = (realTimestamp / 1000);//seconds
		return ((realTimestamp * 10000) + FT_UT_OFFSET);
	}

	/**
	 * Gets a timestamp for item expiration.
	 * 
	 * @param realTimestamp The actual timestamp in milliseconds.
	 * @return The Korean timestamp for the real timestamp.
	 */
	public static int getItemTimestamp(long realTimestamp) {
		int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60); // convert to minutes
		return (int) (time * 35.762787) + ITEM_YEAR2000;
	}

	/**
	 * Gets a timestamp for quest repetition.
	 * 
	 * @param realTimestamp The actual timestamp in milliseconds.
	 * @return The Korean timestamp for the real timestamp.
	 */
	public static int getQuestTimestamp(long realTimestamp) {
		int time = (int) (realTimestamp / 1000 / 60); // convert to minutes
		return (int) (time * 0.1396987) + QUEST_UNIXAGE;
	}
}
