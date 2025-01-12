package com.wops.receiptsgo.filters;

import java.sql.Date;
import java.util.TimeZone;

public class FilterUtils {

	public static boolean isOnOrBefore(Date dt1, TimeZone tz1, Date dt2, TimeZone tz2) {
		long m1 = dt1.getTime();
		long m2 = dt2.getTime();

		return m1 - tz1.getRawOffset() <= m2 - tz2.getRawOffset();
	}

	public static boolean isOnOrAfter(Date dt1, TimeZone tz1, Date dt2, TimeZone tz2) {
		long m1 = dt1.getTime();
		long m2 = dt2.getTime();

		return m1 - tz1.getRawOffset() >= m2 - tz2.getRawOffset();
	}
}
