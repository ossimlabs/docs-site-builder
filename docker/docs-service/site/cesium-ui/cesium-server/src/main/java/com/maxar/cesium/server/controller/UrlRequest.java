package com.maxar.cesium.server.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlRequest
{

	String url;
	ObjectNode body;
	String parent;
	boolean generateParent;
	boolean displayInTree;
	boolean externalService = false;
}
