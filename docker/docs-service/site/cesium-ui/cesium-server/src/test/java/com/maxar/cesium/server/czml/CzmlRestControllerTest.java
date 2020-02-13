package com.maxar.cesium.server.czml;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.packet.Polygon;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

@WebMvcTest
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "com.maxar.cesium.server")
@TestPropertySource(locations = "classpath:testcesiumserver.properties")
public class CzmlRestControllerTest
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	@Qualifier("withEureka")
	private RestTemplate restTemplate;

	@Autowired
	@Qualifier("withoutEureka")
	private RestTemplate externalTemplate;

	final private String testString = "[{" + "    \"id\" : \"redLine\","
			+ "    \"name\" : \"Red line clamped to terain\"," + "    \"polyline\" : {" + "        \"positions\" : {"
			+ "            \"cartographicDegrees\" : [" + "                -75, 35, 0," + "                -125, 35, 0"
			+ "            ]" + "        }," + "        \"material\" : {" + "            \"solidColor\" : {"
			+ "                \"color\" : {" + "                    \"rgba\" : [255, 0, 0, 255]" + "                }"
			+ "            }" + "        }," + "        \"width\" : 5," + "        \"clampToGround\" : true" + "    }"
			+ "}]";

	final private String mockUrl = "/targetService?format=czml";
	final private String body = "{\r\n" + "	\"thisisa\": \"fakebody\",\r\n" + "	\"fordoing\" : \"somegreattesting\"\r\n"
			+ "}";
	final private String bodyProperty = "\"body\":" + body + ",";
	final private String generateParentProperty = "\"generateParent\":\"true\",";

	final private String urlPostNoBody = getUrlPost("",
													false);
	final private String urlPostNoBodyGenParent = getUrlPost(	"",
																true);
	final private String urlPostBody = getUrlPost(	bodyProperty,
													false);

	final private String getUrlPost(
			final String body,
			final boolean generateParent ) {
		final String generateParentString = generateParent ? generateParentProperty : "";
		return "{\r\n" + "	\"url\": \"" + mockUrl + "\",\r\n" + body + generateParentString
				+ "	\"parent\": \"test\"\r\n" + "}";
	}

	@Before
	public void delete()
			throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete("/czml/packets"));
	}

	@Test
	public void czml()
			throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets?parent=test")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(testString))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("redLine"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].parent")
						.value("test"));
	}

	@Test
	public void czmlSession()
			throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets?session=test")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(testString))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets?session=test"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("redLine"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.is4xxClientError());
	}

	@Test
	public void czmlUrlNoBody()
			throws Exception {

		MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo(mockUrl))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(testString,
										MediaType.APPLICATION_JSON));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets/url?parent=test")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(urlPostNoBody))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("redLine"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].parent")
						.value("test"));
	}

	@Test
	public void czmlExtUrlViaQueryParam()
			throws Exception {
		MockRestServiceServer mockServer = MockRestServiceServer.createServer(externalTemplate);
		mockServer.expect(requestTo(mockUrl))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(testString,
										MediaType.APPLICATION_JSON));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets/url?parent=test&externalService=true&url=" + mockUrl)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(urlPostNoBody))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("redLine"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].parent")
						.value("test"));
	}

	@Test
	public void czmlUrlNoBodyMakeParent()
			throws Exception {

		MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo(mockUrl))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(testString,
										MediaType.APPLICATION_JSON));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets/url?parent=test")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(urlPostNoBodyGenParent))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("test"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].name")
						.value("test"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[1].id")
						.value("redLine"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[1].parent")
						.value("test"));
	}

	@Test
	public void czmlUrlNoBody404()
			throws Exception {
		MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo(mockUrl))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets/url?parent=test")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(urlPostNoBody))
				.andExpect(MockMvcResultMatchers.status()
						.is4xxClientError());

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.is4xxClientError());
	}

	@Test
	public void czmlUrlNoBodyNoContent()
			throws Exception {
		MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo(mockUrl))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withNoContent());

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets/url?parent=test")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(urlPostNoBody))
				.andExpect(MockMvcResultMatchers.status()
						.is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.is4xxClientError());
	}

	@Test
	public void czmlUrlBody()
			throws Exception {

		MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo(mockUrl))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json(body))
				.andRespond(withSuccess(testString,
										MediaType.APPLICATION_JSON));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets/url?parent=test")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(urlPostBody))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("redLine"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].parent")
						.value("test"));
	}

	@Test
	public void czmlDeleteTest()
			throws Exception {

		final List<LatLonAlt> pointsList = new ArrayList<>();
		pointsList.add(new LatLonAlt(
				Angle.Zero(),
				Angle.Zero(),
				Length.Zero()));
		pointsList.add(new LatLonAlt(
				Angle.fromDegrees(5),
				Angle.Zero(),
				Length.Zero()));
		pointsList.add(new LatLonAlt(
				Angle.fromDegrees(5),
				Angle.fromDegrees(5),
				Length.Zero()));
		pointsList.add(new LatLonAlt(
				Angle.Zero(),
				Angle.fromDegrees(5),
				Length.Zero()));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("[" + Packet.create()
						.id("DeleteTest")
						.polygon(Polygon.create()
								.positions(PositionList.cartographicDegrees(pointsList))
								.outlineColor(ColorRefValue.color(new Color(
										255,
										0,
										0))))
						.writeString() + "]"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("[" + Packet.create()
						.id("DeleteTest")
						.polygon(Polygon.create()
								.outlineColor(DeletableProperty.createDelete()))
						.writeString() + "]"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("DeleteTest"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0]..rgba.[0]")
						.value(255))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[1]..delete")
						.value(true));
	}

	/*
	 * Packet Hierarchy
	 * @formatter:off
	 *     1      2     3      4
	 *    |  \         |  \
	 *    11  12       31  32
	 *    | \          | \
	 *   111 112      311 312 
	 * @formatter:on
	 * 
	 * Test deletes Packet 1, 2, and 31.
	 * Deleting Packet 1 deletes its whole tree. Deleting packet 31 deletes its two children.
	 * We should be left with Packet 3, its child packet 32, and packet 4, and only these 3 packets
	 */
	@Test
	public void czmlDeletePacketTest()
			throws Exception {
		final String node1Id = "node1";
		final String node11Id = "node11";
		final String node12Id = "node12";
		final String node111Id = "node111";
		final String node112Id = "node112";

		final String node2Id = "node2";

		final String node3Id = "node3";
		final String node31Id = "node31";
		final String node32Id = "node32";
		final String node311Id = "node311";
		final String node312Id = "node312";

		final String node4Id = "node4";

		final JsonNode node1 = makeBasicPacket(	node1Id,
												null);
		final JsonNode node11 = makeBasicPacket(node11Id,
												node1Id);
		final JsonNode node12 = makeBasicPacket(node12Id,
												node1Id);
		final JsonNode node111 = makeBasicPacket(	node111Id,
													node11Id);
		final JsonNode node112 = makeBasicPacket(	node112Id,
													node11Id);

		final JsonNode node2 = makeBasicPacket(	node2Id,
												null);

		final JsonNode node3 = makeBasicPacket(	node3Id,
												null);
		final JsonNode node31 = makeBasicPacket(node31Id,
												node3Id);
		final JsonNode node32 = makeBasicPacket(node32Id,
												node3Id);
		final JsonNode node311 = makeBasicPacket(	node311Id,
													node31Id);
		final JsonNode node312 = makeBasicPacket(	node312Id,
													node31Id);

		final JsonNode node4 = makeBasicPacket(	node4Id,
												null);

		// Add some duplicates to make sure they are all deleted
		final JsonNode duplicate1 = makeBasicPacket(node1Id,
													null);
		final JsonNode duplicate2 = makeBasicPacket(node2Id,
													null);
		final JsonNode duplicate31 = makeBasicPacket(	node31Id,
														null);
		final JsonNode duplicate111 = makeBasicPacket(	node111Id,
														null);
		final JsonNode duplicate311 = makeBasicPacket(	node311Id,
														null);

		final String packets = Arrays.stream(new JsonNode[] {
			node1,
			node11,
			node12,
			duplicate1,
			node111,
			node112,
			node2,
			node3,
			duplicate111,
			node31,
			duplicate2,
			duplicate31,
			node32,
			node311,
			node312,
			node4,
			duplicate311
		})
				.map(JsonNode::toString)
				.collect(Collectors.joining(","));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("[" + packets + "]"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.length()")
						.value("17"));

		final JsonNode deleteNode1 = makeBasicDeletePacket(node1Id);
		final JsonNode deleteNode2 = makeBasicDeletePacket(node2Id);
		final JsonNode deleteNode31 = makeBasicDeletePacket(node31Id);

		final String deletePackets = Arrays.stream(new JsonNode[] {
			deleteNode1,
			deleteNode2,
			deleteNode31
		})
				.map(JsonNode::toString)
				.collect(Collectors.joining(","));

		mockMvc.perform(MockMvcRequestBuilders.post("/czml/packets")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("[" + deletePackets + "]"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/czml/packets"))
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[0].id")
						.value("node3"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[1].id")
						.value("node32"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.[2].id")
						.value("node4"))
				.andExpect(MockMvcResultMatchers.jsonPath("@.length()")
						.value("3"));
	}

	private static JsonNode makeBasicPacket(
			final String id,
			final String parent ) {
		return makeBasicPacket(	id,
								parent,
								false);
	}

	private static JsonNode makeBasicDeletePacket(
			final String id ) {
		return makeBasicPacket(	id,
								null,
								true);
	}

	private static JsonNode makeBasicPacket(
			final String id,
			final String parent,
			final boolean delete ) {
		Packet packet = Packet.create()
				.id(id);
		if (parent != null) {
			packet = packet.parent(parent);
		}

		if (delete) {
			packet = packet.delete(delete);
		}

		return packet.toJsonNode();
	}
}
