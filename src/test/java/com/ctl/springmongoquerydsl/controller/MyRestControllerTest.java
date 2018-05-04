package com.ctl.springmongoquerydsl.controller;

import com.ctl.springmongoquerydsl.dao.PersonRepository;
import com.ctl.springmongoquerydsl.model.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MyRestControllerTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    protected MockMvc mvc;

    @Test
    public void testFind() throws Exception {
        personRepository.insert(Arrays.asList(
                Person.builder().name("Stan").family("Marsh").build(),
                Person.builder().name("Randy").family("Marsh").build(),
                Person.builder().name("Sharon").family("Marsh").build(),
                Person.builder().name("Eric").family("Cartman").build()
        ));

        mvc.perform(get("/rest/all")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        final String jsonPredicate = "{\"$and\":[{\"person.family\":{\"$containsIc\": \"Marsh\"}}, {\"person.name\" : {\"$containsIc\": \"Stan\"}}]}";

        mvc.perform(post("/rest/find")
                .content(jsonPredicate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name", is("Stan")))
                .andExpect(jsonPath("$.[0].family", is("Marsh")))
        ;
    }
}