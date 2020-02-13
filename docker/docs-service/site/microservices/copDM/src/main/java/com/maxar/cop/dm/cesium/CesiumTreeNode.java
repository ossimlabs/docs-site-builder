package com.maxar.cop.dm.cesium;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.cesium.czmlwriter.Packet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CesiumTreeNode
{
	private String id;
	private String name;
	private String parentId;

	public JsonNode toCzml() {
		Packet packet = Packet.create()
				.id(id)
				.name(name);
		if (parentId != null) {
			packet = packet.parent(parentId);
		}
		return packet.toJsonNode();
	}
}
