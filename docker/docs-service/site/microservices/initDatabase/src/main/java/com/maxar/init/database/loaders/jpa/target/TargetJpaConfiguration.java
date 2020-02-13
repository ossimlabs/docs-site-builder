package com.maxar.init.database.loaders.jpa.target;

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
@EnableJpaRepositories(basePackages = "com.maxar.target.repository", entityManagerFactoryRef = "targetEntityManagerFactory", transactionManagerRef = "targetTransactionManager")
public class TargetJpaConfiguration
{
	@Bean
	@ConfigurationProperties("target.datasource")
	public DataSourceProperties targetDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "target.datasource.configuration")
	public DataSource targetDataSource() {
		return targetDataSourceProperties().initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
	}

	@Bean(name = "targetEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean targetEntityManagerFactory(
			final EntityManagerFactoryBuilder builder ) {
		return builder.dataSource(targetDataSource())
				.packages(	"com.maxar.target.repository",
							"com.maxar.target.entity")
				.build();
	}

	@Bean
	public PlatformTransactionManager targetTransactionManager(
			@Qualifier("targetEntityManagerFactory")
			final EntityManagerFactory targetEntityManagerFactory ) {
		return new JpaTransactionManager(
				targetEntityManagerFactory);
	}
}
