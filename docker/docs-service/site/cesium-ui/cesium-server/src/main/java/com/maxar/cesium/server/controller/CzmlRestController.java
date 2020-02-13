package com.maxar.cesium.server.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.LoadingCache;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.PacketWriter;
import com.maxar.cesium.czmlwriter.packet.LayerControl;

import cesiumlanguagewriter.PacketCesiumWriter;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "czml")
public class CzmlRestController
{
	public final static String DEFAULT_SESSION = "default";

	private final static String KEEP_ALIVE_MESSAGE = "KEEP_ALIVE";

	@Autowired
	public SimpMessagingTemplate template;

	@Autowired
	@Qualifier("withEureka")
	public RestTemplate restTemplate;

	@Autowired
	@Qualifier("withoutEureka")
	public RestTemplate extRestTemplate;

	@Autowired
	LoadingCache<String, List<JsonNode>> sessionCache;

	ObjectMapper mapper = new ObjectMapper();

	@GetMapping(path = "/packets", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Gets all Posted CZML in the session")
	public ResponseEntity<String> getSessionPackets(
			@RequestParam(value = "session", defaultValue = DEFAULT_SESSION)
			final String sessionId ) {
		final List<JsonNode> sessionCzml = sessionCache.getIfPresent(sessionId);

		if (sessionCzml != null) {
			return ResponseEntity.ok(Arrays.toString(sessionCzml.toArray()));
		}
		else {
			return ResponseEntity.notFound()
					.build();
		}

	}

	@GetMapping(path = "/resetSessionTimeout")
	@ApiOperation(value = "Resets CZML cache eviction timeout for specified session")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Eviction timeout for this session CZML was reset"),
		@ApiResponse(code = 404, message = "No CZML currently exists for this session"),
	})
	public ResponseEntity<String> resetCzmlEvictionTimeout(
			@RequestParam(value = "session")
			final String sessionId ) {

		if (sessionCache.getIfPresent(sessionId) != null) {
			return ResponseEntity.ok("Session " + sessionId + " CZML cache eviction timeout has been reset");
		}
		else {
			return ResponseEntity.notFound()
					.build();
		}

	}

	@PostMapping(path = "/packets", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Adds several CZML packets to the session and sends it onto the WebSocket")
	public ResponseEntity<Boolean> postPackets(
			@RequestParam(value = "session", defaultValue = DEFAULT_SESSION)
			final String sessionId,
			@RequestParam(required = false)
			final String parent,
			@RequestParam(defaultValue = "false")
			final boolean generateParent,
			@RequestParam(defaultValue = "true")
			final boolean displayInTree,
			@RequestBody
			final List<JsonNode> czml ) {

		if (parent != null) {
			czml.stream()
					.filter(node -> !node.hasNonNull(PacketCesiumWriter.ParentPropertyName))
					.forEach(node -> ((ObjectNode) node).put(	PacketCesiumWriter.ParentPropertyName,
																parent));

			if (!displayInTree) {
				czml.stream()
						.forEach(node -> {
							final ObjectNode layerNode = mapper.createObjectNode();
							layerNode.put(	LayerControl.SHOW_AS_LAYER_PROPERTY_NAME,
											false);
							((ObjectNode) node).set(PacketWriter.LAYER_CONTROL_PROPETY_NAME,
													layerNode);
						});
			}

			if (generateParent) {
				czml.add(	0,
							makeParent(parent));
			}
		}

		final Set<String> idsToCreateDeletePacketsFor = addCzmlToSession(	sessionId,
																			czml);
		final List<JsonNode> newDeletePackets = createDeletePacketsForIds(idsToCreateDeletePacketsFor);
		czml.addAll(newDeletePackets);

		sendCzmlToWebSocket(sessionId,
							Arrays.toString(czml.toArray()));

		return ResponseEntity.ok()
				.body(true);
	}

	private JsonNode makeParent(
			final String parentId ) {
		final ObjectNode parentNode = new ObjectMapper().createObjectNode();
		parentNode.put(	PacketCesiumWriter.IdPropertyName,
						parentId);
		parentNode.put(	PacketCesiumWriter.NamePropertyName,
						parentId);
		return parentNode;
	}

	private Set<String> addCzmlToSession(
			final String sessionId,
			final List<JsonNode> czml ) {
		final List<JsonNode> packets;
		final List<JsonNode> sessionCzml = sessionCache.getIfPresent(sessionId);

		if (sessionCzml != null) {
			packets = sessionCzml;
		}
		else {
			packets = new ArrayList<>();
			sessionCache.put(	sessionId,
								packets);
		}

		// Instead of sending a bunch of packets that will eventually get deleted,
		// remove packets for which we've received a delete packet, and don't add the
		// delete packet

		// Split packets by regular packets, and packet level delete packets
		final Map<Boolean, List<JsonNode>> nodesByDelete = czml.stream()
				.collect(Collectors.groupingBy(jsonNode -> jsonNode.path(PacketCesiumWriter.DeletePropertyName)
						.asBoolean(false)));

		// Get the ids for the delete packets
		final Set<String> idsToRemove = nodesByDelete.getOrDefault(	true,
																	Collections.emptyList())
				.stream()
				.filter(jsonNode -> jsonNode.has(PacketCesiumWriter.IdPropertyName))
				.map(jsonNode -> jsonNode.get(PacketCesiumWriter.IdPropertyName)
						.asText())
				.collect(Collectors.toSet());

		final Set<String> childIdsToRemove = deleteCzmlFromSession(	packets,
																	idsToRemove);
		// We will need to insert delete packets for the children of deleted packets
		// because the client won't do that automatically, but make sure to remove ids
		// for packets we already have delete packets for
		childIdsToRemove.removeAll(idsToRemove);

		// Add all the regular packets
		packets.addAll(nodesByDelete.getOrDefault(	false,
													Collections.emptyList()));

		return childIdsToRemove;
	}

	private Set<String> deleteCzmlFromSession(
			final List<JsonNode> packets,
			final Set<String> idsToRemove ) {

		if (!idsToRemove.isEmpty()) {

			// Remove all packets that have an id of one of the delete packets
			packets.removeIf(jsonNode -> jsonNode.has(PacketCesiumWriter.IdPropertyName)
					&& idsToRemove.contains(jsonNode.get(PacketCesiumWriter.IdPropertyName)
							.asText()));

			// Find any ids that list the deleted packets as a parent
			final Set<String> childIdsToRemove = packets.stream()
					.filter(packet -> packet.has(PacketCesiumWriter.ParentPropertyName)
							&& idsToRemove.contains(packet.get(PacketCesiumWriter.ParentPropertyName)
									.asText()))
					.map(jsonNode -> jsonNode.get(PacketCesiumWriter.IdPropertyName)
							.asText())
					.collect(Collectors.toSet());

			// Now remove those packets too
			childIdsToRemove.addAll(deleteCzmlFromSession(	packets,
															childIdsToRemove));

			return childIdsToRemove;
		}
		else {
			return Collections.emptySet();
		}
	}

	private List<JsonNode> createDeletePacketsForIds(
			final Set<String> idsToDelete ) {
		return idsToDelete.stream()
				.map(id -> {
					return Packet.create()
							.id(id)
							.delete(true)
							.toJsonNode();

				})
				.collect(Collectors.toList());
	}

	private void sendCzmlToWebSocket(
			final String sessionId,
			final String czml ) {
		template.convertAndSend("/topic/czml/" + sessionId,
								czml);
	}

	private void sendKeepAliveMessageToSessionClient(
			final String sessionId ) {
		template.convertAndSend("/topic/czml/" + sessionId,
								KEEP_ALIVE_MESSAGE);
	}

	@GetMapping(path = "/packets/url")
	@ApiOperation(value = "Send a url in a get for the serive to call to retrieve several CZML packets to the session")
	public ResponseEntity<Boolean> getPacketsWithUrl(
			@RequestParam(value = "session", defaultValue = DEFAULT_SESSION)
			final String sessionId,
			@RequestParam
			final String url,
			@RequestParam(defaultValue = "false")
			final boolean externalService,
			@RequestParam(required = false)
			final String parent,
			@RequestParam(defaultValue = "false")
			final boolean generateParent,
			@RequestParam(defaultValue = "true")
			final boolean displayInTree )
			throws RestClientException,
			URISyntaxException {

		final UrlRequest urlRequest = new UrlRequest(
				url,
				null,
				parent,
				generateParent,
				displayInTree,
				externalService);

		return postPacketsUrl(	sessionId,
								urlRequest);
	}

	@PostMapping(path = "/packets/url", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Post a url for the serive to call to retrieve several CZML packets to the session")
	public ResponseEntity<Boolean> postPacketsUrl(
			@RequestParam(value = "session", defaultValue = DEFAULT_SESSION)
			final String sessionId,
			@RequestBody
			final UrlRequest requestUrlData )
			throws RestClientException,
			URISyntaxException {

		HttpMethod httpMethod = HttpMethod.GET;
		HttpEntity<ObjectNode> requestEntity = null;

		if (requestUrlData.getBody() != null) {
			httpMethod = HttpMethod.POST;
			requestEntity = new HttpEntity<>(
					requestUrlData.getBody());
		}

		try {
			final RestTemplate restTemplateToUse = requestUrlData.externalService ? extRestTemplate : restTemplate;

			final ResponseEntity<List<JsonNode>> entity = restTemplateToUse.exchange(	new URI(
					requestUrlData.getUrl()),
																						httpMethod,
																						requestEntity,
																						new ParameterizedTypeReference<List<JsonNode>>() {});
			if (entity.getStatusCode()
					.is2xxSuccessful()) {
				if (entity.getBody() == null) {
					return ResponseEntity.status(entity.getStatusCode())
							.body(true);
				}
				return postPackets(	sessionId,
									requestUrlData.getParent(),
									requestUrlData.isGenerateParent(),
									requestUrlData.isDisplayInTree(),
									entity.getBody());
			}
			else {
				return ResponseEntity.status(entity.getStatusCode())
						.body(false);
			}
		}
		catch (final HttpClientErrorException ex) {
			return ResponseEntity.status(ex.getStatusCode())
					.body(false);
		}
	}

	@DeleteMapping(path = "/packets")
	@ApiOperation(value = "Clears all packets from the session")
	public ResponseEntity<Boolean> deletePackets(
			@RequestParam(value = "session", defaultValue = DEFAULT_SESSION)
			final String sessionId ) {
		final List<JsonNode> removed = sessionCache.getIfPresent(sessionId);

		if (removed != null) {
			sessionCache.invalidate(sessionId);

			sendCzmlToWebSocket(sessionId,
								"");
			return ResponseEntity.ok()
					.body(true);
		}
		else {
			return ResponseEntity.notFound()
					.build();
		}
	}

	/**
	 * Worker method to send out 'keep alive' message to all session clients.
	 *
	 * In order for the client session's CZML cache to be refreshed, an asynchronous
	 * response is expected.
	 * 
	 * This method is configured to run at a set interval set by the
	 * "czml.cesiumserver.scheduling.interval-milliseconds" configuration option.
	 */
	@Scheduled(fixedRateString = "${czml.cesiumserver.scheduling.interval-milliseconds}")
	private void sendSessionClientKeepAliveMessage() {
		final Set<String> sessionIds = sessionCache.asMap()
				.keySet();

		for (final String sessionId : sessionIds) {
			sendKeepAliveMessageToSessionClient(sessionId);
		}
	}
}
