package com.maxar.init.database.loaders.jpa.asset;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.maxar.asset.repository", entityManagerFactoryRef = "assetEntityManagerFactory", transactionManagerRef = "assetTransactionManager")
public class AssetJpaConfiguration
{
	@Bean
	@ConfigurationProperties("asset.datasource")
	public DataSourceProperties assetDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "asset.datasource.configuration")
	public DataSource assetDataSource() {
		return assetDataSourceProperties().initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
	}

	@Bean(name = "assetEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean assetEntityManagerFactory(
			final EntityManagerFactoryBuilder builder ) {
		return builder.dataSource(assetDataSource())
				.packages(	"com.maxar.asset.repository",
							"com.maxar.asset.entity")
				.build();
	}

	@Bean
	public PlatformTransactionManager assetTransactionManager(
			@Qualifier("assetEntityManagerFactory")
			final EntityManagerFactory assetEntityManagerFactory ) {
		return new JpaTransactionManager(
				assetEntityManagerFactory);
	}
}
