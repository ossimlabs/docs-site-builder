package com.maxar.common.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import com.radiantblue.analytics.core.log.SourceLogger;

public class RestTemplateErrorHandler implements
		ResponseErrorHandler
{
	private static Logger logger = SourceLogger
			.getLogger(
					new Object() {}.getClass().getEnclosingClass().getName());

	@Override
	public void handleError(
			final ClientHttpResponse httpResponse )
			throws IOException {

		if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
			logger
					.error(
							"RestTemplate call has Server Error:" + httpResponse.getStatusCode());
		}
		else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
			logger
					.error(
							"RestTemplate call has Client Error: " + httpResponse.getStatusCode());
		}
		else {
			logger
					.error(
							"RestTemplate call has Other Error");
		}
	}

	@Override
	public boolean hasError(
			final ClientHttpResponse httpResponse )
			throws IOException {

		return ((httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR)
				|| (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR));
	}

}
