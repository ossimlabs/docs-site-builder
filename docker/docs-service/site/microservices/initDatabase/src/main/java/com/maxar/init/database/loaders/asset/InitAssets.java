package com.maxar.init.database.loaders.asset;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import com.maxar.asset.common.utils.MultipartFileToString;
import com.maxar.init.database.loaders.asset.service.AssetAirborneService;
import com.maxar.init.database.loaders.asset.service.AssetSpaceService;
import com.radiantblue.analytics.core.log.SourceLogger;

@EnableAutoConfiguration
@EnableJpaRepositories("com.maxar.asset.repository")
@EntityScan("com.maxar.asset.entity")
@ComponentScan(basePackages = {
	"com.maxar.init.database.loaders.asset.service",
	"com.maxar.asset.common.service"
})
public class InitAssets implements
		CommandLineRunner
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private AssetSpaceService assetSpaceService;

	@Autowired
	private AssetAirborneService assetAirborneService;

	@Value("${microservices.asset.space.xmlDirectory}")
	private String spaceAssetDirNameProp;

	@Value("${microservices.asset.airborne.xmlDirectory}")
	private String airborneAssetDirNameProp;

	public void initAssets() {
		final FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(
					final File dir,
					final String name ) {
				return name.endsWith("xml");
			}
		};

		String spaceAssetDirName = spaceAssetDirNameProp;

		if (!spaceAssetDirName.startsWith("/")) {
			final URL spaceUrl = Thread.currentThread()
					.getContextClassLoader()
					.getResource(spaceAssetDirNameProp);

			if (spaceUrl == null) {
				throw new RuntimeException(
						"Cannot find resource on classpath: '" + spaceAssetDirNameProp + "'");
			}

			spaceAssetDirName = spaceUrl.getFile();
		}

		final File spaceDir = new File(
				spaceAssetDirName);

		if (!spaceDir.exists()) {
			logger.error(spaceAssetDirName + " does not exist");
		}

		final File[] spaceFiles = spaceDir.listFiles(filter);

		for (final File file : spaceFiles) {
			logger.info("Space asset:" + file.getName());
			try {
				final String name = file.getName();
				final byte[] content = Files.readAllBytes(file.toPath());
				final MockMultipartFile mock = new MockMultipartFile(
						name,
						content);
				assetSpaceService.createAsset(MultipartFileToString.multipartFileToString(mock));
			}
			catch (final XmlBeanDefinitionStoreException e) {
				logger.error("XmlBeanDefinitionStoreException create space asset");
				e.printStackTrace();
			}
			catch (final IOException e) {
				logger.error("IOException create space asset");
				e.printStackTrace();
			}
		}

		String airborneAssetDirName = airborneAssetDirNameProp;

		if (!airborneAssetDirName.startsWith("/")) {
			final URL airborneUrl = Thread.currentThread()
					.getContextClassLoader()
					.getResource(airborneAssetDirNameProp);
			if (airborneUrl == null) {
				throw new RuntimeException(
						"Cannot find resource on classpath: '" + airborneAssetDirNameProp + "'");
			}

			airborneAssetDirName = airborneUrl.getFile();
		}

		final File airborneDir = new File(
				airborneAssetDirName);

		if (!airborneDir.exists()) {
			logger.error(airborneAssetDirName + " does not exist");
		}

		final File[] airborneFiles = airborneDir.listFiles(filter);

		for (final File file : airborneFiles) {
			logger.info("Airborne asset:" + file.getName());
			try {
				final String name = file.getName();
				final byte[] content = Files.readAllBytes(file.toPath());
				final MockMultipartFile mock = new MockMultipartFile(
						name,
						content);
				assetAirborneService.createAsset(MultipartFileToString.multipartFileToString(mock));
			}
			catch (final XmlBeanDefinitionStoreException e) {
				logger.error("XmlBeanDefinitionStoreException create airborne asset");
				e.printStackTrace();
			}
			catch (final IOException e) {
				logger.error("IOException create airborne asset");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		initAssets();
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitAssets.class).properties("spring.config.name:initassets")
						.build()
						.run(args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
