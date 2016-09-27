package com.bazlur.jobseek.utils;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/28/16.
 */
public class StringUtils {
	public static boolean contains(final String haystack, final String needle) {
		String haystackTemp = ((haystack == null) ? "" : haystack).toLowerCase();
		String needleTemp = (needle == null ? "" : needle).toLowerCase();

		return haystackTemp.contains(needleTemp);
	}

	public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	public static boolean isNotEmpty(Object str) {
		return !isEmpty(str);
	}
}
