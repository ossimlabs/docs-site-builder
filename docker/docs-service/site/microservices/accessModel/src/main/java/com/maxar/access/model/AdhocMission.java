package com.maxar.access.model;

import java.util.List;

import com.maxar.mission.model.TrackNodeModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * The constraint object for constraints provided in the access generation
 * request.
 */
@Data
@ApiModel
public class AdhocMission
{
	public final static String exampleOnStation = "2020-01-01T12:00:00Z";
	public final static String exampleOffStation = "2020-01-01T17:00:00Z";
	public final static String exampleAltitude = "18000";
	public final static String exampleWaypoints = "[{\"sequence\": 1," + 
			"\"wayPoint\": \"POINT (25.35454592577163 51.412643524825256)\"," + 
			"\"offsetMillis\": 0}," + 
			"{\"sequence\": 2," + 
			"\"wayPoint\": \"POINT (27.04384610777648 50.49306870646909)\"," + 
			"\"offsetMillis\": 876280}," + 
			"{\"sequence\": 3," + 
			"\"wayPoint\": \"POINT (28.63438958210105 49.39444466118572)\"," + 
			"\"offsetMillis\": 1811430}," + 
			"{\"sequence\": 4," + 
			"\"wayPoint\": \"POINT (29.42800318026324 48.85595914190689)\"," + 
			"\"offsetMillis\": 2277354}," + 
			"{\"sequence\": 5," + 
			"\"wayPoint\": \"POINT (29.65269773379994 49.61663068816056)\"," + 
			"\"offsetMillis\": 2759216}," + 
			"{\"sequence\": 6," + 
			"\"wayPoint\": \"POINT (28.75945931873673 50.38096525773517)\"," + 
			"\"offsetMillis\": 3354442}," + 
			"{\"sequence\": 7," + 
			"\"wayPoint\": \"POINT (27.745512546794032 51.03208272627231)\"," + 
			"\"offsetMillis\": 3924090}," + 
			"{\"sequence\": 8," + 
			"\"wayPoint\": \"POINT (27.382544277304973 51.80339489218484)\"," + 
			"\"offsetMillis\": 4424324}," + 
			"{\"sequence\": 9," + 
			"\"wayPoint\": \"POINT (26.82652358431552 52.62989788941)\"," + 
			"\"offsetMillis\": 4980798}," + 
			"{\"sequence\": 10," + 
			"\"wayPoint\": \"POINT (26.33743426778382 53.76474648984529)\"," + 
			"\"offsetMillis\": 5710348}," + 
			"{\"sequence\": 11," + 
			"\"wayPoint\": \"POINT (26.193044498911043 54.51638545357165)\"," + 
			"\"offsetMillis\": 6181183}," + 
			"{\"sequence\": 12," + 
			"\"wayPoint\": \"POINT (26.2673806015427 55.47511268654)\"," + 
			"\"offsetMillis\": 6778632}," + 
			"{\"sequence\": 13," + 
			"\"wayPoint\": \"POINT (26.36945243845922 56.009556719420296)\"," + 
			"\"offsetMillis\": 7113320}," + 
			"{\"sequence\": 14," + 
			"\"wayPoint\": \"POINT (26.629336929918875 56.55786038255977)\"," + 
			"\"offsetMillis\": 7466407}," + 
			"{\"sequence\": 15," + 
			"\"wayPoint\": \"POINT (26.530229310526284 56.81618889365873)\"," + 
			"\"offsetMillis\": 7630822}," + 
			"{\"sequence\": 16," + 
			"\"wayPoint\": \"POINT (26.09086525233509 56.81402824192118)\"," + 
			"\"offsetMillis\": 7780889}," + 
			"{\"sequence\": 17," + 
			"\"wayPoint\": \"POINT (25.899468690615134 55.81713135473698)\"," + 
			"\"offsetMillis\": 8405165}," + 
			"{\"sequence\": 18," + 
			"\"wayPoint\": \"POINT (25.853974775623552 54.53496935290007)\"," + 
			"\"offsetMillis\": 9203557}," + 
			"{\"sequence\": 19," + 
			"\"wayPoint\": \"POINT (26.09940917833477 53.67085445377099)\"," + 
			"\"offsetMillis\": 9748875}," + 
			"{\"sequence\": 20," + 
			"\"wayPoint\": \"POINT (26.615745265172528 52.45511683075398)\"," + 
			"\"offsetMillis\": 10529847}," + 
			"{\"sequence\": 21," + 
			"\"wayPoint\": \"POINT (27.176777679316352 51.53412391769698)\"," + 
			"\"offsetMillis\": 11142096}," + 
			"{\"sequence\": 22," + 
			"\"wayPoint\": \"POINT (27.58154804406904 50.60235718056082)\"," + 
			"\"offsetMillis\": 11743102}," + 
			"{\"sequence\": 23," + 
			"\"wayPoint\": \"POINT (28.658298677032807 50.07773534240721)\"," + 
			"\"offsetMillis\": 12281845}," + 
			"{\"sequence\": 24," + 
			"\"wayPoint\": \"POINT (29.56105136794559 49.903441273706186)\"," + 
			"\"offsetMillis\": 12659752}," + 
			"{\"sequence\": 25," + 
			"\"wayPoint\": \"POINT (29.344509396981636 49.39637778580869)\"," + 
			"\"offsetMillis\": 12987040}," + 
			"{\"sequence\": 26," + 
			"\"wayPoint\": \"POINT (27.09315733890915 50.79057240272936)\"," + 
			"\"offsetMillis\": 14237376}," + 
			"{\"sequence\": 27," + 
			"\"wayPoint\": \"POINT (25.30354053024232 51.47332062080555)\"," + 
			"\"offsetMillis\": 15056596}]";
	
	@ApiModelProperty(position = 0, required = true, example = exampleOnStation, notes = "On-station time, in ISO8601 format")
	private String onStationTime;
	
	@ApiModelProperty(position = 1, required = true, example = exampleOffStation, notes = "Off-station time, in ISO8601 format")
	private String offStationTime;

	@ApiModelProperty(position = 2, required = true, example = exampleWaypoints, notes = "Track nodes")
	private List<TrackNodeModel> waypoints;

	@ApiModelProperty(position = 3, required = true, example = exampleAltitude, notes = "Track altitude in meters")
	private Double altitudeMeters;
}
