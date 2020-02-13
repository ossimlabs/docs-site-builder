package com.maxar.asset.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.radiantblue.analytics.isr.core.model.asset.Asset;

public class InMemoryResourceLocationTest
{
	private List<String> entriesAdded;

	@Before
	public void setUp() {
		entriesAdded = new ArrayList<>();
	}

	@After
	public void tearDown() {
		entriesAdded.forEach(InMemoryResourceLocation::removeFromResourceMap);
	}

	@Test
	public void testOpenStream() throws
			IOException {
		final String resourceLocation = "TEST_LOCATION";
		final String resourceData = "TEST_DATA";

		final InMemoryResourceLocation inMemoryResourceLocation = new InMemoryResourceLocation();
		inMemoryResourceLocation.setLocation(resourceLocation);

		InMemoryResourceLocation.addToResourceMap(resourceLocation,
												  resourceData.getBytes());
		entriesAdded.add(resourceLocation);

		final InputStream inputStream = inMemoryResourceLocation.openStream();

		Assert.assertNotNull(inputStream);

		final byte[] inputStreamBytes = inputStream.readAllBytes();

		Assert.assertNotNull(inputStreamBytes);
		Assert.assertArrayEquals(resourceData.getBytes(),
								 inputStreamBytes);
	}

	@Test(expected = NullPointerException.class)
	public void testOpenStreamNoEntry() {
		final String resourceLocation = "TEST_LOCATION";

		final InMemoryResourceLocation inMemoryResourceLocation = new InMemoryResourceLocation();
		inMemoryResourceLocation.setLocation(resourceLocation);

		inMemoryResourceLocation.openStream();
	}

	@Test(expected = NullPointerException.class)
	public void testOpenStreamNullEntry() {
		final String resourceLocation = "TEST_LOCATION";

		final InMemoryResourceLocation inMemoryResourceLocation = new InMemoryResourceLocation();
		inMemoryResourceLocation.setLocation(resourceLocation);

		InMemoryResourceLocation.addToResourceMap(resourceLocation,
												  null);
		entriesAdded.add(resourceLocation);

		inMemoryResourceLocation.openStream();
	}

	@Test
	public void testCanonicalPath() {
		final String resourceLocation = "TEST_LOCATION";

		final InMemoryResourceLocation inMemoryResourceLocation = new InMemoryResourceLocation();
		inMemoryResourceLocation.setLocation(resourceLocation);

		Assert.assertEquals(resourceLocation,
							inMemoryResourceLocation.canonicalPath());
	}

	@Test
	public void testCreateAsset() {
		final String resourceLocation = "IN_MEMORY:sar_file.txt";
		final String resourceData = "HEADER ROW\n"
				+ "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"
				+ "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"
				+ "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"
				+ "0 0 0 4.0 0 0 0 20 1.0 0 0 20 1.0 0 0 20 1.0 0 0\n";

		InMemoryResourceLocation.addToResourceMap(resourceLocation,
												  resourceData.getBytes());
		entriesAdded.add(resourceLocation);

		final GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load("/RS02_fileref.xml");
		applicationContext.refresh();

		final Asset asset = (Asset) applicationContext.getBean("RS02");
		applicationContext.close();

		Assert.assertNotNull(asset);

		asset.init();

		Assert.assertEquals("RS02",
							asset.getName());
		Assert.assertEquals(32382,
							asset.getId());
	}

	@Test(expected = NullPointerException.class)
	public void testCreateAssetNoEntry() {
		final GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load("/RS02_fileref.xml");
		applicationContext.refresh();

		final Asset asset = (Asset) applicationContext.getBean("RS02");
		applicationContext.close();

		Assert.assertNotNull(asset);

		asset.init();
	}
}
