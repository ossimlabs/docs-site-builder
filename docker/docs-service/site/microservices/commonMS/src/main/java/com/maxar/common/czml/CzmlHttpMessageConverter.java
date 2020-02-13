package com.maxar.common.czml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class CzmlHttpMessageConverter implements
		HttpMessageConverter<Object>
{
	public static final MediaType APPLICATION_CZML = new MediaType(
			"application",
			"czml");

	@Autowired
	private List<CzmlTypeHandler> handlers;

	// This message convert is only for converter to CZML, not converting from
	@Override
	public boolean canRead(
			final Class<?> clazz,
			final MediaType mediaType ) {
		return false;
	}

	// If a class implements CzmlProducer, this converter can write it
	@Override
	public boolean canWrite(
			final Class<?> clazz,
			final MediaType mediaType ) {
		return (handlers != null) && !handlers.isEmpty() && (Iterable.class.isAssignableFrom(clazz) || handlers.stream()
				.filter(handler -> handler.canHandle(clazz))
				.findAny()
				.isPresent()) && ((mediaType == null) || getSupportedMediaTypes().contains(mediaType));
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return Arrays.asList(APPLICATION_CZML);
	}

	// This class doesn't do any reading
	@Override
	public Object read(
			final Class<? extends Object> clazz,
			final HttpInputMessage inputMessage )
			throws IOException,
			HttpMessageNotReadableException {
		return null;
	}

	@Override
	public void write(
			final Object czmlProducer,
			final MediaType arg1,
			final HttpOutputMessage outputMessage )
			throws IOException,
			HttpMessageNotWritableException {

		outputMessage.getHeaders()
				.setContentType(MediaType.APPLICATION_JSON_UTF8);

		try (final OutputStream outputStream = outputMessage.getBody()) {

			final String body;
			if (czmlProducer instanceof Iterable<?>) {
				final Iterator<?> iterator = ((Iterable<?>) czmlProducer).iterator();
				if (iterator.hasNext()) {
					final Object firstObject = iterator.next();

					body = handlers.stream()
							.filter(handler -> handler.handlesIterable() && handler.canHandle(firstObject.getClass()))
							.findFirst()
							.map(handler -> Arrays.toString(handler.handle(czmlProducer)
									.stream()
									.filter(czml -> ((czml != null) && (!czml.isEmpty())))
									.toArray()))
							.orElseGet(() -> {
								return handlers.stream()
										.filter(handler -> handler.canHandle(firstObject.getClass()))
										.findFirst()
										.map(handler -> handleIterable(	((Iterable<?>) czmlProducer).iterator(),
																		handler))
										.orElseThrow(() -> {
											return new HttpMessageNotWritableException(
													"No valid CzmlTypeHandler found for type "
															+ czmlProducer.getClass());
										});
							});

				}
				else {
					body = "[ ]";
				}
			}
			else {
				body = Arrays.toString(handlers.stream()
						.filter(handler -> handler.canHandle(czmlProducer.getClass()))
						.findFirst()
						.orElseThrow(() -> {
							return new HttpMessageNotWritableException(
									"No valid CzmlTypeHandler found for type " + czmlProducer.getClass());
						})
						.handle(czmlProducer)
						.stream()
						.filter(czml -> ((czml != null) && (!czml.isEmpty())))
						.toArray());
			}

			outputStream.write(body.getBytes());
		}

	}

	private String handleIterable(
			final Iterator<? extends Object> iterator,
			final CzmlTypeHandler handler ) {
		final ArrayList<String> czmls = new ArrayList<>();
		while (iterator.hasNext()) {
			czmls.addAll(handler.handle(iterator.next())
					.stream()
					.filter(czml -> ((czml != null) && (!czml.isEmpty())))
					.collect(Collectors.toList()));
		}

		return Arrays.toString(czmls.toArray());
	}

	public List<CzmlTypeHandler> getHandlers() {
		return handlers;
	}

	public void setHandlers(
			final List<CzmlTypeHandler> handlers ) {
		this.handlers = handlers;
	}
}
