package com.maxar.cesium.czmlwriter.packet;

import org.joda.time.DateTime;

import com.maxar.cesium.czmlwriter.PacketWriter;

public interface TimelineControl
{
	public static final String ID_PROPERTY_NAME = "id";
	public static final String START_PROPERTY_NAME = "start";
	public static final String END_PROPERTY_NAME = "end";
	public static final String CONTENT_PROPERTY_NAME = "content";
	public static final String STYLE_PROPERTY_NAME = "style";
	public static final String GROUP_PROPERTY_NAME = "group";


	
	public void write(
			PacketWriter writer );

	public static TimelineControl create() {
		return writer -> {};
	}
	
	default TimelineControl id(
			final String id ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	ID_PROPERTY_NAME,
									id);
		};
	}
	
	default TimelineControl start(
			final DateTime start ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	START_PROPERTY_NAME,
									start);
		};
	}
	
	default TimelineControl end(
			final DateTime end ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	END_PROPERTY_NAME,
			                     	end);
		};
	}
	
	default TimelineControl content(
			final String content ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	CONTENT_PROPERTY_NAME,
			                     	content);
		};
	}
	
	default TimelineControl style(
			final String style ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	STYLE_PROPERTY_NAME,
			                     	style);
		};
	}
	
	default TimelineControl group(
			final String group ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	GROUP_PROPERTY_NAME,
			                     	group);
		};
	}
}
