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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.mission.model.MissionModel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "mission", indexes = {
	@Index(name = "IDX_mission_id", columnList = "id"),
	@Index(name = "IDX_mission_name", columnList = "name"),
	@Index(name = "IDX_mission_assetid", columnList = "assetid")
})
public class Mission implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "missionkey", columnDefinition = "UUID")
	@JsonIgnore
	private UUID missionKey;

	private String id;
	private String name;
	
	@Column(name = "assetid")
	private String assetId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trackkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Track track;

	@Column(name = "onstationmillis")
	private Long onStationMillis;
	
	@Column(name = "offstationmillis")
	private Long offStationMillis;
	
	private Double speedMPH;
	
	@Column(name = "altitudemeters")
	private Double altitudeMeters;

	public Mission(
			final String id,
			final String name,
			final String assetId,
			final Track track,
			final Long onStationMillis,
			final Long offStationMillis,
			final Double speedMPH,
			final Double altitudeMeters ) {
		super();
		this.id = id;
		this.name = name;
		this.assetId = assetId;
		this.track = track;
		this.onStationMillis = onStationMillis;
		this.offStationMillis = offStationMillis;
		this.speedMPH = speedMPH;
		this.altitudeMeters = altitudeMeters;
	}
	
	public MissionModel toModel() {
		return MissionModel.builder()
				.id(id)
				.name(name)
				.assetId(assetId)
				.track(track.toModel())
				.onStationMillis(onStationMillis)
				.offStationMillis(offStationMillis)
				.speedMPH(speedMPH)
				.altitudeMeters(altitudeMeters)
				.build();
	}
}
