package com.ef.utils;

import java.util.Date;

/**
 * Date utils class
 * 
 * @author victor
 *
 */
public class DateUtils {
	private static final long ONE_MINUTE_IN_MILLIS = 60000;// milliseconds

	public static Date oneDayAfter(Date date) {
		return new Date(date.getTime() + (60 * 24 * ONE_MINUTE_IN_MILLIS));
	}

	public static Date oneHourAfter() {
		return oneHourAfter(new Date());
	}

	public static Date oneHourAfter(Date date) {
		return new Date(date.getTime() + (60 * ONE_MINUTE_IN_MILLIS));
	}

	public static Date oneHourBefore() {
		return oneHourBefore(new Date());
	}

	public static Date oneHourBefore(Date date) {
		return new Date(date.getTime() - (60 * ONE_MINUTE_IN_MILLIS));
	}
}
