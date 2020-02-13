package com.maxar.mission.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.maxar.mission.model.TrackNodeModel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tracknode", indexes = {
	@Index(name = "IDX_tracknode_sequence", columnList = "sequence")
})
public class TrackNode implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "tracknodekey", columnDefinition = "UUID")
	@JsonIgnore
	private UUID trackNodeKey;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trackkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Track track;

	private int sequence;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	@Column(name = "waypoint")
	private Point wayPoint;
	
	// cumulative offset from mission start
	@Column(name = "offsetmillis")
	private Long offsetMillis;
	private boolean required = false;

	public TrackNode(
			final Track track,
			final int sequence,
			final Point wayPoint,
			final Long offsetMillis,
			final boolean required ) {
		super();
		this.track = track;
		this.sequence = sequence;
		this.wayPoint = wayPoint;
		this.offsetMillis = offsetMillis;
		this.required = required;
	}

	public TrackNodeModel toModel() {
		return TrackNodeModel.builder()
				.sequence(sequence)
				.wayPoint(wayPoint)
				.offsetMillis(offsetMillis)
				.required(required)
				.build();
	}
}
