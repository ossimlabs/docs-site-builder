package com.maxar.cesium.czmlwriter.refvalue;

import java.awt.image.RenderedImage;
import java.net.URI;

import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;

import cesiumlanguagewriter.CesiumImageFormat;
import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.UriCesiumWriter;
import cesiumlanguagewriter.advanced.ICesiumUriResolver;

public interface UriRefValue extends
		Property<UriCesiumWriter>,
		DeletableProperty<UriCesiumWriter, UriRefValue>
{
	public static UriRefValue uri(
			final String uri,
			final CesiumResourceBehavior resource ) {
		return writer -> writer.writeUri(	uri,
											resource);
	}

	public static UriRefValue uri(
			final String uri,
			final ICesiumUriResolver resolver ) {
		return writer -> writer.writeUri(	uri,
											resolver);
	}

	public static UriRefValue uri(
			final URI uri,
			final CesiumResourceBehavior resource ) {
		return writer -> writer.writeUri(	uri,
											resource);
	}

	public static UriRefValue uri(
			final URI uri,
			final ICesiumUriResolver resolver ) {
		return writer -> writer.writeUri(	uri,
											resolver);
	}

	public static UriRefValue uri(
			final RenderedImage image ) {
		return writer -> writer.writeUri(image);
	}

	public static UriRefValue uri(
			final RenderedImage image,
			final CesiumImageFormat format ) {
		return writer -> writer.writeUri(	image,
											format);
	}

	public static UriRefValue reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}

	@Override
	default public UriRefValue wrap(
			final Property<UriCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}