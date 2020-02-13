package com.maxar.cop.dm.cesium;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class CesiumTreeNodeTest
{
	private static final String CESIUM_TREE_NODE_ID = "CESIUM_TREE_NODE_ID";
	private static final String CESIUM_TREE_NODE_NAME = "CESIUM_TREE_NODE_NAME";
	private static final String CESIUM_TREE_NODE_PARENT_ID = "CESIUM_TREE_NODE_PARENT_ID";

	@Test
	public void testEqualsAndHashCode() {
		final CesiumTreeNode node1 = new CesiumTreeNode(
				CESIUM_TREE_NODE_ID,
				CESIUM_TREE_NODE_NAME,
				CESIUM_TREE_NODE_PARENT_ID);
		final CesiumTreeNode node2 = new CesiumTreeNode(
				CESIUM_TREE_NODE_ID,
				CESIUM_TREE_NODE_NAME,
				CESIUM_TREE_NODE_PARENT_ID);

		Assert.assertEquals(true,
							node1.canEqual(node2));
		Assert.assertEquals(true,
							node1.equals(node2));
		Assert.assertEquals(node1.hashCode(),
							node2.hashCode());
	}

	@Test
	public void testSetters() {
		final CesiumTreeNode node1 = new CesiumTreeNode(
				CESIUM_TREE_NODE_ID,
				CESIUM_TREE_NODE_NAME,
				CESIUM_TREE_NODE_PARENT_ID);
		final CesiumTreeNode node2 = new CesiumTreeNode(
				"",
				"",
				"");
		node2.setId(CESIUM_TREE_NODE_ID);
		node2.setName(CESIUM_TREE_NODE_NAME);
		node2.setParentId(CESIUM_TREE_NODE_PARENT_ID);

		Assert.assertEquals(true,
							node1.equals(node2));
		Assert.assertEquals(node1.hashCode(),
							node2.hashCode());
	}

	@Test
	public void testToString() {
		final CesiumTreeNode node = new CesiumTreeNode(
				CESIUM_TREE_NODE_ID,
				CESIUM_TREE_NODE_NAME,
				CESIUM_TREE_NODE_PARENT_ID);
		final String expectedString = "CesiumTreeNode(id=" + CESIUM_TREE_NODE_ID + ", name=" + CESIUM_TREE_NODE_NAME
				+ ", parentId=" + CESIUM_TREE_NODE_PARENT_ID + ")";

		Assert.assertEquals(expectedString,
							node.toString());
	}

	@Test
	public void testToCzml() {
		final CesiumTreeNode node = new CesiumTreeNode(
				CESIUM_TREE_NODE_ID,
				CESIUM_TREE_NODE_NAME,
				CESIUM_TREE_NODE_PARENT_ID);

		final JsonNode jsonNode = node.toCzml();

		Assert.assertNotNull(jsonNode);
	}
}
