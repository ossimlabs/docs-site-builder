package com.maxar.planning.entity.tasking;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.planning.model.tasking.TaskingModel;
import com.maxar.planning.entity.link.Link;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tasking")
public class Tasking implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "taskingkey", columnDefinition = "UUID")
	@JsonIgnore
	protected UUID taskingKey;

	@Column(name = "missionid")
	private String missionId;

	@Column(name = "taskingcoord")
	private Geometry taskingCoord;

	@Column(name = "lookatcoord")
	private Geometry lookAtCoord;

	@Column(name = "sensorname")
	private String sensorName;

	@Column(name = "sensortype")
	private String sensorType;

	@Column(name = "sensormode")
	private String sensorMode;

	@Column(name = "sensorped")
	private Integer sensorPed;

	@Column(name = "sensoradhoc")
	private Integer sensorAdHoc;

	@Column(name = "sensorslots")
	private Integer sensorSlots;

	@Column(name = "earliestimagetimemillis")
	private Long earliestImageTimeMillis;

	@Column(name = "latestimagetimemillis")
	private Long latestImageTimeMillis;

	@Column(name = "numimages")
	private Integer numImages;

	@Column(name = "priority")
	private Integer priority;

	@Column(name = "tottimemillis")
	private long totTimeMillis;

	@Column(name = "taskingauthority")
	private String taskingAuthority;

	@Column(name = "taskstandadhocflag")
	private String taskStandingAdHocFlag;

	@Column(name = "scene")
	private String scene;

	@Column(name = "complex")
	private Boolean complex;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "linkkey", referencedColumnName = "linkkey")
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@EqualsAndHashCode.Exclude
	private Link link;

	public Tasking(
			String missionId,
			Geometry taskingCoord,
			Geometry lookAtCoord,
			String sensorName,
			String sensorType,
			String sensorMode,
			Integer sensorPed,
			Integer sensorAdHoc,
			Integer sensorSlots,
			Long earliestImageTimeMillis,
			Long latestImageTimeMillis,
			Integer numImages,
			Integer priority,
			long totTimeMillis,
			String taskingAuthority,
			String taskStandingAdHocFlag,
			String scene,
			Boolean complex,
			Link link ) {
		super();
		this.missionId = missionId;
		this.taskingCoord = taskingCoord;
		this.lookAtCoord = lookAtCoord;
		this.sensorName = sensorName;
		this.sensorType = sensorType;
		this.sensorMode = sensorMode;
		this.sensorPed = sensorPed;
		this.sensorAdHoc = sensorAdHoc;
		this.sensorSlots = sensorSlots;
		this.earliestImageTimeMillis = earliestImageTimeMillis;
		this.latestImageTimeMillis = latestImageTimeMillis;
		this.numImages = numImages;
		this.priority = priority;
		this.totTimeMillis = totTimeMillis;
		this.taskingAuthority = taskingAuthority;
		this.taskStandingAdHocFlag = taskStandingAdHocFlag;
		this.scene = scene;
		this.complex = complex;
		this.link = link;
	}
	
	public TaskingModel toModel() {
		return TaskingModel.builder()
				.missionId(missionId)
				.taskingCoord(taskingCoord)
				.lookAtCoord(lookAtCoord)
				.sensorName(sensorName)
				.sensorType(sensorType)
				.sensorMode(sensorMode)
				.sensorPed(sensorPed)
				.sensorAdHoc(sensorAdHoc)
				.sensorSlots(sensorSlots)
				.earliestImageTimeMillis(earliestImageTimeMillis)
				.latestImageTimeMillis(latestImageTimeMillis)
				.numImages(numImages)
				.priority(priority)
				.totTimeMillis(totTimeMillis)
				.taskingAuthority(taskingAuthority)
				.taskStandingAdHocFlag(taskStandingAdHocFlag)
				.scene(scene)
				.complex(complex)
				.link(link.toModel())
				.build();
	}
}
