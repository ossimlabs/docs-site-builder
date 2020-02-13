package com.maxar.target.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public class Target implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "targetkey", columnDefinition = "UUID")
	@JsonIgnore
	private UUID targetKey;

	@Column(name = "targettype")
	@Enumerated
	protected TargetType targetType;

	@Column(name = "targetid")
	protected String targetId;

	@Column(name = "targetname")
	protected String targetName;

	protected String description;

	@Column(name = "countrycode")
	protected String countryCode;

	@Column(name = "georegion")
	protected String geoRegion;

	@Column(name = "orderofbattle")
	@Enumerated
	protected OrderOfBattle orderOfBattle = OrderOfBattle.GROUND;

	protected Geometry geometry;

	// TODO: Do we need the following fields?:
	// updatetimemillis -
	// mapgridkey - we don't think so since we can just pass geometry to the
	// weather service, which I believe is the only reason we had the mapgrids
	// was for the weather
	// groupkey -
	// terrainmaskkey -

	public Target(
			final Target other ) {
		this(
				other.targetType,
				other.targetId,
				other.targetName,
				other.description,
				other.countryCode,
				other.geoRegion,
				other.orderOfBattle,
				other.geometry);
	}

	public Target(
			final TargetType targetType ) {
		this.targetType = targetType;
	}

	public Target(
			final TargetType targetType,
			final String targetId,
			final String targetName,
			final String description,
			final String countryCode,
			final String geoRegion,
			final OrderOfBattle orderOfBattle ) {
		this(
				targetType);
		this.targetId = targetId;
		this.targetName = targetName;
		this.description = description;
		this.countryCode = countryCode;
		this.geoRegion = geoRegion;
		if (orderOfBattle == null) {
			this.orderOfBattle = OrderOfBattle.GROUND;
		}
		else {
			this.orderOfBattle = orderOfBattle;
		}
	}

	public Target(
			final TargetType targetType,
			final String targetId,
			final String targetName,
			final String description,
			final String countryCode,
			final String geoRegion,
			final OrderOfBattle orderOfBattle,
			final Geometry geometry ) {
		this(
				targetType);
		this.targetId = targetId;
		this.targetName = targetName;
		this.description = description;
		this.countryCode = countryCode;
		this.geoRegion = geoRegion;
		if (orderOfBattle == null) {
			this.orderOfBattle = OrderOfBattle.GROUND;
		}
		else {
			this.orderOfBattle = orderOfBattle;
		}
		this.geometry = geometry;
	}

	@JsonIgnore
	public Geometry getGeometry() {
		return geometry;
	}

	@Column(name = "geometry")
	@JsonProperty("geometry")
	public String getGeometryAsWkt() {
		final WKTWriter writer = new WKTWriter();
		return writer.writeFormatted(geometry);
	}

	public void setGeometry(
			final Geometry geometry ) {
		this.geometry = geometry;
	}

	@Override
	public String toString() {
		return String.format(	"Target[%s]: %s %s %s",
								targetId,
								targetName,
								countryCode,
								geoRegion);
	}
	
	public TargetModel toModel() {
		return toModel(null, null);
	}
	
	public TargetModel toModel(DateTime czmlStartTime, DateTime czmlStopTime) {
		return TargetModel.builder()
				.targetType(targetType)
				.targetId(targetId)
				.targetName(targetName)
				.description(description)
				.countryCode(countryCode)
				.geoRegion(geoRegion)
				.orderOfBattle(orderOfBattle)
				.geometry(geometry)
				.czmlStartTime(czmlStartTime)
				.czmlStopTime(czmlStopTime)
				.build();
	}
}
