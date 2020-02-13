package com.maxar.cop.dm.cesium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.cop.dm.service.CopServiceClient;

@Component
public class CesiumSession
{
	@Autowired
	private CopServiceClient serviceClient;

	@Value("${microservices.copdm.parentNodeCreationDelayMillis}")
	private long parentNodeCreationDelayMillis;

	private final Map<String, DateTime> existingParentNodes = new HashMap<>();
	private final List<CesiumTreeNode> nodesToSubmit = new ArrayList<>();

	public synchronized void addTreeNode(
			final CesiumTreeNode node ) {
		nodesToSubmit.add(node);
	}

	public void submitTreeNodes() {
		for (final CesiumTreeNode node : nodesToSubmit) {
			serviceClient.createParentNode(node);
			existingParentNodes.put(node.getId(),
									DateTime.now());
		}
	}

	private synchronized long getParentNodeDelay(
			final String parentId ) {
		if (existingParentNodes.containsKey(parentId)) {
			final long age = DateTime.now()
					.getMillis()
					- existingParentNodes.get(parentId)
							.getMillis();
			if (age < parentNodeCreationDelayMillis) {
				return parentNodeCreationDelayMillis - age;
			}
			else {
				return 0;
			}
		}
		else {
			existingParentNodes.put(parentId,
									DateTime.now());
			return -1;
		}
	}

	public void ensureParentNodeExists(
			final String parentId ) {
		ensureParentNodeExists(new CesiumTreeNode(
				parentId,
				parentId,
				null));
	}

	public void ensureParentNodeExists(
			final CesiumTreeNode parentNode ) {
		long wait = getParentNodeDelay(parentNode.getId());
		if (wait < 0) {
			serviceClient.createParentNode(parentNode);
			wait = parentNodeCreationDelayMillis;
		}

		if (wait > 0) {
			try {
				Thread.sleep(wait);
			}
			catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread()
						.interrupt();
			}
		}
	}

	public ObjectNode createCzmlRequest(
			final String requestUrl,
			final Object requestBody,
			final String parentPacketId,
			final boolean generateParent ) {
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode request = mapper.createObjectNode();
		request.set("url",
					mapper.convertValue(requestUrl,
										JsonNode.class));
		if (requestBody != null) {
			request.set("body",
						mapper.convertValue(requestBody,
											JsonNode.class));
		}
		if (parentPacketId != null) {
			request.set("parent",
						mapper.convertValue(parentPacketId,
											JsonNode.class));
			if (generateParent) {
				ensureParentNodeExists(parentPacketId);
			}
		}
		return request;
	}

	public ObjectNode createCzmlRequest(
			final String requestUrl,
			final Object requestBody,
			final String parentPacketId ) {
		return createCzmlRequest(	requestUrl,
									requestBody,
									parentPacketId,
									true);
	}
}
