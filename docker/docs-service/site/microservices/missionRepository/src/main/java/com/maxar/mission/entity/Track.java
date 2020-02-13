package com.maxar.mission.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.maxar.mission.model.TrackModel;
import com.maxar.mission.model.TrackNodeModel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "track", indexes = {
	@Index(name = "IDX_track_id", columnList = "id"),
	@Index(name = "IDX_track_name", columnList = "name")
})
@JsonIgnoreProperties({
	"hibernateLazyInitializer",
	"handler"
})
public class Track implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "trackkey", columnDefinition = "UUID")
	@JsonIgnore
	private UUID trackKey;

	private String id;
	private String name;

	@OneToMany(mappedBy = "track", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TrackNode> trackNodes = new ArrayList<>();

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	@Column(name = "trackgeo")
	private Geometry trackGeo;

	public Track(
			final String id,
			final String name ) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public TrackModel toModel() {
		List<TrackNodeModel> nodeModels = trackNodes
				.stream()
				.map(TrackNode::toModel)
				.collect(Collectors.toList());
		
		return TrackModel.builder()
				.id(id)
				.name(name)
				.trackNodes(nodeModels)
				.trackGeo(trackGeo)
				.build();
	}
}
