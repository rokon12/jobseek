package com.bazlur.jobseek.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/28/16.
 */
public class JobUrlCache {
	private static JobUrlCache ourInstance = new JobUrlCache();

	public static JobUrlCache getInstance() {
		return ourInstance;
	}

	private static Set<String> cache = new HashSet<>();

	public void add(String url) {
		cache.add(url);
	}

	public void remove(String url) {
		cache.remove(url);
	}

	public Set<String> getCache() {
		return cache;
	}

	private JobUrlCache() {
	}
}
