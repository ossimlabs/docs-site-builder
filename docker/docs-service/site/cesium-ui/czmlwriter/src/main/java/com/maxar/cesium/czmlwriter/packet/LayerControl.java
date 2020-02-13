package com.maxar.cesium.czmlwriter.packet;

import org.joda.time.DateTime;

import com.maxar.cesium.czmlwriter.PacketWriter;

public interface LayerControl
{
	public static final String LAYER_NAME_PROPERTY_NAME = "layerName";
	public static final String SHOW_AS_LAYER_PROPERTY_NAME = "showAsLayer";
	public static final String ZOOM_TO_CHILD_PROPERTY_NAME = "zoomToChild";
	public static final String ZOOM_TO_CHILD_ID_PROPERTY_NAME = "zoomToChildId";
	public static final String ZOOM_START_PROPERTY_NAME = "zoomStart";
	public static final String ZOOM_END_PROPERTY_NAME = "zoomEnd";

	public void write(
			PacketWriter writer );

	public static LayerControl create() {
		return writer -> {};
	}

	default LayerControl layerName(
			final String layerName ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	LAYER_NAME_PROPERTY_NAME,
									layerName);
		};
	}

	default LayerControl showAsLayer(
			final boolean showAsLayer ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	SHOW_AS_LAYER_PROPERTY_NAME,
									showAsLayer);
		};
	}

	default LayerControl zoomToChild(
			final boolean zoomToChild ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	ZOOM_TO_CHILD_PROPERTY_NAME,
									zoomToChild);
		};
	}

	default LayerControl zoomToChildId(
			final String zoomToChildId ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	ZOOM_TO_CHILD_ID_PROPERTY_NAME,
									zoomToChildId);
		};
	}

	default LayerControl zoomStart(
			final DateTime zoomStart ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	ZOOM_START_PROPERTY_NAME,
									zoomStart);
		};
	}
}
