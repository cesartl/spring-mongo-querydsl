package com.ctl.springmongoquerydsl;

import com.ctl.springmongoquerydsl.jackson.DefaultQueryDslDeserializer;
import com.ctl.springmongoquerydsl.model.QPerson;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class GeneralConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();
        b.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        b.deserializers(new DefaultQueryDslDeserializer(QPerson.class));
        b.modules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
        return b;
    }


}
