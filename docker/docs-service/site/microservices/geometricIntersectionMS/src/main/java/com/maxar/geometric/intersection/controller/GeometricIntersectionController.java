package com.maxar.geometric.intersection.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.geometric.intersection.exception.AreaOfInterestIdDoesNotExistException;
import com.maxar.geometric.intersection.service.GeometricIntersectionService;
import com.maxar.geometric.intersection.model.AreaOfInterest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/geometry")
public class GeometricIntersectionController
{
	@Autowired
	private GeometricIntersectionService geometricIntersectionService;

	@CrossOrigin
	@PostMapping(value = "")
	@ApiOperation("Creates a new geometric area of interest")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The new geometry was successfully added"),
		@ApiResponse(code = 400, message = "There was a problem with the request")
	})
	public ResponseEntity<String> createGeometry(
			@RequestBody
			final AreaOfInterest areaOfInterest ) {
		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		final URI uri = URI.create("/geometry/" + id);

		return ResponseEntity.created(uri)
				.build();
	}

	@CrossOrigin
	@PostMapping(value = "/ui")
	@ApiOperation("Creates a new geometric area of interest using polygons in the UI format")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The new geometry was successfully added"),
		@ApiResponse(code = 400, message = "There was a problem with the request")
	})
	public ResponseEntity<List<String>> createGeometryUi(
			@RequestBody
			final List<List<List<Double>>> polygons ) {
		final List<String> ids = new ArrayList<>();

		for (final List<List<Double>> polygon : polygons) {
			final String coordinatesWkt = polygon.stream()
					.map(coordinates -> coordinates.stream()
							.map(coordinate -> Optional.ofNullable(coordinate)
									.orElse(0.0))
							.map(String::valueOf)
							.collect(Collectors.joining(" ")))
					.collect(Collectors.joining(", "));

			final String geometryWkt = "POLYGON ((" + coordinatesWkt + "))";

			final AreaOfInterest areaOfInterest = new AreaOfInterest();
			areaOfInterest.setGeometryWkt(geometryWkt);

			final String id = geometricIntersectionService.createGeometry(areaOfInterest);
			ids.add(id);
		}

		return ResponseEntity.ok(ids);
	}

	@CrossOrigin
	@GetMapping(value = "")
	@ApiOperation("Gets the IDs of all existing areas of interest")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The area of interest IDs were successfully retrieved")
	})
	public ResponseEntity<List<String>> getGeometries() {
		final List<String> ids = geometricIntersectionService.getGeometries();

		return ResponseEntity.ok(ids);
	}

	@CrossOrigin
	@GetMapping(value = "/{id}")
	@ApiOperation("Gets an existing area of operation")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The area of interest was successfully retrieved"),
		@ApiResponse(code = 400, message = "The requested ID is not a valid ID"),
		@ApiResponse(code = 401, message = "There is no event with the requested ID"),
	})
	public ResponseEntity<AreaOfInterest> getGeometryById(
			@ApiParam(value = "The ID of the geometry to retrieve", example = "test-aoi-01")
			@PathVariable(name = "id")
			final String id )
			throws AreaOfInterestIdDoesNotExistException {
		final AreaOfInterest areaOfInterest = geometricIntersectionService.getGeometryById(id);

		return ResponseEntity.ok(areaOfInterest);
	}

	@CrossOrigin
	@GetMapping(value = "/intersect")
	@ApiOperation("Gets areas of interest intersecting the input geometry")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The areas of interest were successfully retrieved"),
		@ApiResponse(code = 400, message = "There was a problem with the request")
	})
	public ResponseEntity<List<AreaOfInterest>> getIntersectingGeometries(
			@ApiParam(value = "The geometry (WKT) to query for", example = "POLYGON ((30 25, 31 25, 31 24, 30 24, 30 25))")
			@RequestParam(name = "geometry")
			final String geometry ) {
		final List<AreaOfInterest> areasOfInterest = geometricIntersectionService.getIntersectingGeometries(geometry);

		return ResponseEntity.ok(areasOfInterest);
	}

	@CrossOrigin
	@DeleteMapping(value = "/{id}")
	@ApiOperation("Deletes an existing area of interest")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The area of interest was successfully deleted"),
		@ApiResponse(code = 400, message = "The requested ID is not a valid ID"),
		@ApiResponse(code = 401, message = "There is no event with the requested ID"),
	})
	public ResponseEntity<String> deleteGeometryById(
			@ApiParam(value = "The ID of the geometry to retrieve", example = "test-aoi-01")
			@PathVariable(name = "id")
			final String id )
			throws AreaOfInterestIdDoesNotExistException {
		geometricIntersectionService.deleteGeometryById(id);

		return ResponseEntity.ok()
				.build();
	}
}
