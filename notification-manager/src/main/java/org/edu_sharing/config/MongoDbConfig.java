package org.edu_sharing.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoDbConfig {

//    public DefaultMongoTypeMapper typeMapper(){
//        return new DefaultMongoTypeMapper("type");
//    }
//
//    @Bean
//    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory, MongoCustomConversions customConversions, MongoMappingContext mappingContext){
//        DbRefResolver dbRefResolver = new DefaultDbRefResolver(databaseFactory);
//        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
//        converter.setCustomConversions(customConversions);
//        converter.setCodecRegistryProvider(databaseFactory);
//        converter.setTypeMapper(typeMapper());
//        return converter;
//    }
}
