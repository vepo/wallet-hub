package com.ef.utils;

public class StringUtils {
	public static String first(String... s) {
		for (String _s : s) {
			if (_s != null && !_s.isEmpty()) {
				return _s;
			}
		}
		return null;
	}
}
