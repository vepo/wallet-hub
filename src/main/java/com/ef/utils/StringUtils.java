package com.ef.utils;

/**
 * String utils
 * 
 * @author victor
 *
 */
public class StringUtils {
	/**
	 * Retrieve the first String that is not null or empty
	 * 
	 * @param s
	 *            any string
	 * 
	 * @return The first string that is not null or empty, or null if all null or
	 *         empty
	 */
	public static String first(String... s) {
		for (String _s : s) {
			if (_s != null && !_s.isEmpty()) {
				return _s;
			}
		}
		return null;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
