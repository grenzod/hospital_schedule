package com.example.BackEnd.Config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
       basePackages = "com.example.BackEnd.Repositories.mysql",
       entityManagerFactoryRef = "mysqlEntityManagerFactory",
       transactionManagerRef = "mysqlTransactionManager"
)
public class MySQLDatabaseConfiguration {

   @Bean
   @Primary
   @ConfigurationProperties("spring.datasource")
   public DataSourceProperties mysqlDataSourceProperties() {
       return new DataSourceProperties();
   }

   @Bean
   @Primary
   public DataSource mysqlDataSource() {
       return mysqlDataSourceProperties()
               .initializeDataSourceBuilder()
               .build();
   }

   @Bean
   @Primary
   public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
           EntityManagerFactoryBuilder builder) {
       return builder
               .dataSource(mysqlDataSource())
               .packages("com.example.BackEnd.Entity")
               .persistenceUnit("mysqlPU")
               .build();
   }

   @Bean
   @Primary
   public PlatformTransactionManager mysqlTransactionManager(
           EntityManagerFactory mysqlEntityManagerFactory) {
       return new JpaTransactionManager(mysqlEntityManagerFactory);
   }
}
