package com.maxar.common.csv;

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

public class CsvHttpMessageConverter implements
		HttpMessageConverter<Object>
{
	public static final MediaType TEXT_CSV = new MediaType(
			"text",
			"csv");

	@Autowired
	private List<CsvTypeHandler<?>> handlers;

	// This message convert is only for converter to Csv, not converting from
	@Override
	public boolean canRead(
			final Class<?> clazz,
			final MediaType mediaType ) {
		return false;
	}

	@Override
	public boolean canWrite(
			final Class<?> clazz,
			final MediaType mediaType ) {
		return handlers != null && !handlers.isEmpty() && Iterable.class.isAssignableFrom(clazz)
				&& (mediaType == null || getSupportedMediaTypes().contains(mediaType));
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return Arrays.asList(TEXT_CSV);
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
			final Object csvProducer,
			final MediaType arg1,
			final HttpOutputMessage outputMessage )
			throws IOException,
			HttpMessageNotWritableException {

		outputMessage.getHeaders()
				.setContentType(MediaType.APPLICATION_JSON_UTF8);

		try (final OutputStream outputStream = outputMessage.getBody()) {

			final Iterator<?> iterator = ((Iterable<?>) csvProducer).iterator();
			final String body;

			if (iterator.hasNext()) {
				final Object firstObject = iterator.next();

				body = handlers.stream()
						.filter(handler -> handler.canHandle(firstObject.getClass()))
						.findFirst()
						.map(handler -> handleIterable(	((Iterable<?>) csvProducer).iterator(),
														handler))
						.orElseThrow(() -> {
							return new HttpMessageNotWritableException(
									"No valid CzmlTypeHandler found for type " + csvProducer.getClass());
						});

			}
			else {
				body = "";
			}

			outputStream.write(body.getBytes());
		}

	}

	private String handleIterable(
			final Iterator<? extends Object> iterator,
			CsvTypeHandler<?> handler ) {
		final ArrayList<String> csvs = new ArrayList<>();
		while (iterator.hasNext()) {
			csvs.addAll(handler.handleObject(iterator.next()));
		}

		csvs.add(	0,
					handler.headers());

		return csvs.stream()
				.collect(Collectors.joining("\n"));
	}

	public List<CsvTypeHandler<?>> getHandlers() {
		return handlers;
	}

	public void setHandlers(
			List<CsvTypeHandler<?>> handlers ) {
		this.handlers = handlers;
	}
}
