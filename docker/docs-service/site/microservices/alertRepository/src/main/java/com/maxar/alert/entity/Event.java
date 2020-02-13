package com.maxar.alert.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The repository model class for events.
 *
 * This class inherits from the API level event that is ingested into the Alert
 * Service. That class contains a geometryWkt field, which is the geometry of
 * the event in well-known text (WKT). To handle the translation between these
 * two fields without having to parse WKT each time this class's geometry field
 * is set, some extra work is required.
 *
 * This class contains a constructor that accepts an API event object, which is
 * used to fill in the fields of the new repository model event's parent, and
 * then the geometryWkt is used to instantiate the JTS geometry field. This is
 * the way conversions from the API event to the repository model event should
 * be done.
 *
 * This class also contains a method named updateGeometryWkt, which will set the
 * geometryWkt field to be the JTS geometry field's text form (which is WKT).
 * This is the way conversions from the repository model event to the API event
 * should be done.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(name = "events")
public class Event extends
		com.maxar.alert.model.Event
{
	@Column(name = "geometry")
	@JsonIgnore
	private Geometry geometry;

	/**
	 * Construct from an API level event.
	 *
	 * This constructor will instantiate the parent event based on the input event,
	 * and will use that event's geometryWkt to construct this event's geometry
	 * object.
	 *
	 * @param event
	 *            The API event object.
	 */
	public Event(
			final com.maxar.alert.model.Event event ) {
		super(
				event.getType(),
				event.getStartTime(),
				event.getEndTime(),
				event.getGeometryWkt(),
				event.getSource(),
				event.getCountry(),
				event.getId());

		final WKTReader reader = new WKTReader();
		try {
			geometry = reader.read(getGeometryWkt());
		}
		catch (final ParseException e) {
			// Throwing a RuntimeException is unchecked, so this constructor can
			// be used in a stream map operation.
			throw new RuntimeException(
					e);
		}
	}

	/**
	 * Sets the geometryWkt field to be the geometry field's WKT.
	 */
	public void updateGeometryWkt() {
		setGeometryWkt(geometry.toText());
	}

	@Column(name = "id")
	@Id
	@Override
	public String getId() {
		return super.getId();
	}

	@Column(name = "type")
	@Override
	public String getType() {
		return super.getType();
	}

	@Column(name = "starttime")
	@Override
	public DateTime getStartTime() {
		return super.getStartTime();
	}

	@Column(name = "endtime")
	@Override
	public DateTime getEndTime() {
		return super.getEndTime();
	}

	@Column(name = "source")
	@Override
	public String getSource() {
		return super.getSource();
	}

	@Column(name = "country")
	@Override
	public String getCountry() {
		return super.getCountry();
	}
}
