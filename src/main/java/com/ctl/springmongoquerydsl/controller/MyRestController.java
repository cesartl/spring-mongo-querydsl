package com.ctl.springmongoquerydsl.controller;

import com.ctl.springmongoquerydsl.dao.PersonRepository;
import com.ctl.springmongoquerydsl.model.Person;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest")
public class MyRestController {

    private final PersonRepository personRepository;

    @Autowired
    public MyRestController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @PostMapping(path = "/find")
    public Iterable<Person> findPersons(@RequestBody Predicate predicate) {
        return personRepository.findAll(predicate);
    }

    @GetMapping(path = "/all")
    public Iterable<Person> findALl(){
        return personRepository.findAll();
    }
}
