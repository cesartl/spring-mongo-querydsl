package com.ctl.springmongoquerydsl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Person {
    @Id
    private String id;

    @Field
    private String name;

    @Field
    private int age;

    @Field
    private String family;
}
