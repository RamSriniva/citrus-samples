/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.todolist;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;
import static com.consol.citrus.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    @Qualifier("inboundDictionary")
    private DataDictionary<?> inboundDictionary;

    @Autowired
    @Qualifier("outboundDictionary")
    private DataDictionary<?> outboundDictionary;

    @Test
    @CitrusTest
    public void testJsonPayloadValidation() {
        variable("todoId", "citrus:randomUUID()");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .dictionary(outboundDictionary)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .dictionary(inboundDictionary)
            .body("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}"));
    }

    @Test
    @CitrusTest
    public void testJsonValidationWithFileResource() {
        variable("todoId", "citrus:randomUUID()");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
             .message()
            .type(MessageType.JSON)
            .dictionary(outboundDictionary)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body(new ClassPathResource("templates/todo.json")));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .dictionary(inboundDictionary)
            .body(new ClassPathResource("templates/todo.json")));
    }

    @Test
    @CitrusTest
    public void testJsonPathValidation() {
        variable("todoId", "citrus:randomUUID()");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .dictionary(outboundDictionary)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .dictionary(inboundDictionary)
            .validate(jsonPath()
                    .expression("$.id", "${todoId}")
                    .expression("$.title", "todo_${todoId}")
                    .expression("$.description", "@endsWith('todo_${todoId}')@")
                    .expression("$.done", false)));
    }

}