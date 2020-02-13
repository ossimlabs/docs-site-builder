package com.maxar.weather.entity.map;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mapgrid")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public class MapGrid implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "mapgridkey", columnDefinition = "UUID")
	@JsonIgnore
	protected UUID mapGridKey;

	private String id;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry geometry;

	public MapGrid(
			final String id ) {
		this.id = id;
	}

	public MapGrid(
			final String id,
			final Geometry geometry ) {
		this.id = id;
		this.geometry = geometry;
	}
}
