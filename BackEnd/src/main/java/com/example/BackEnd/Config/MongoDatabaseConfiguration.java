package com.example.BackEnd.Config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(
       basePackages    = "com.example.BackEnd.Repositories.mongodb",
       mongoTemplateRef = "mongoTemplate"
)
public class MongoDatabaseConfiguration {
   @Bean
   @Primary
   @ConfigurationProperties(prefix = "spring.data.mongodb")
   public MongoProperties mongoProps() {
       return new MongoProperties();
   }

   @Bean
   public MongoClient mongoClient(MongoProperties props) {
       return MongoClients.create(props.getUri());
   }

   @Bean(name = "mongoTemplate")
   public MongoTemplate mongoTemplate(MongoClient client,
           MongoProperties props) {
       return new MongoTemplate(client, props.getDatabase());
   }
}
