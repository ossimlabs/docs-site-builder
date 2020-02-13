package com.maxar.asset.common.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.radiantblue.analytics.core.io.ResourceLocation;

/**
 * A ResourceLocation implementation that keeps data in memory, rather than
 * loading from a file.
 *
 * The location field must be set, and the static resourceMap must be filled
 * with a resource buffer with a key name the same as the location field before
 * the openStream method is invoked on this object. The resource buffer with the
 * corresponding key will be loaded into a ByteArrayInputStream and returned by
 * the openStream method.
 */
public class InMemoryResourceLocation extends
		ResourceLocation
{
	private static final Map<String, byte[]> resourceMap = new ConcurrentHashMap<>();

	@Override
	public InputStream openStream() {
		final byte[] resourceBuffer = resourceMap.get(getLocation());

		return new ByteArrayInputStream(resourceBuffer);
	}

	@Override
	public String canonicalPath() {
		return getLocation();
	}

	/**
	 * Add or update an entry to the resource map.
	 *
	 * @param key The key for the entry.
	 * @param buffer The data for the entry.
	 */
	public static void addToResourceMap(final String key,
										final byte[] buffer) {
		resourceMap.put(key,
						buffer);
	}

	/**
	 * Remove an entry from the resource map.
	 * @param key The key for the entry to remove.
	 */
	public static void removeFromResourceMap(final String key) {
		resourceMap.remove(key);
	}
}
