package com.ctl.springmongoquerydsl.dao;

import com.ctl.springmongoquerydsl.model.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PersonRepository extends MongoRepository<Person, String>, QuerydslPredicateExecutor<Person> {
}
