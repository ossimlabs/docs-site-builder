package com.maxar.init.database.loaders.jpa.ephemeris;

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
@EnableJpaRepositories(basePackages = "com.maxar.ephemeris.repository", entityManagerFactoryRef = "ephemerisEntityManagerFactory", transactionManagerRef = "ephemerisTransactionManager")
public class EphemerisJpaConfiguration
{
	@Bean
	@ConfigurationProperties("ephemeris.datasource")
	public DataSourceProperties ephemerisDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "ephemeris.datasource.configuration")
	public DataSource ephemerisDataSource() {
		return ephemerisDataSourceProperties().initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
	}

	@Bean(name = "ephemerisEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean ephemerisEntityManagerFactory(
			final EntityManagerFactoryBuilder builder ) {
		return builder.dataSource(ephemerisDataSource())
				.packages(	"com.maxar.ephemeris.repository",
							"com.maxar.ephemeris.entity")
				.build();
	}

	@Bean
	public PlatformTransactionManager ephemerisTransactionManager(
			@Qualifier("ephemerisEntityManagerFactory")
			final EntityManagerFactory ephemerisEntityManagerFactory ) {
		return new JpaTransactionManager(
				ephemerisEntityManagerFactory);
	}
}
