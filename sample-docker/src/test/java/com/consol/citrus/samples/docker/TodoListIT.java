/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.samples.docker;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.docker.actions.DockerExecuteAction.Builder.docker;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends AbstractDockerIT {

    @Autowired
    private DockerClient dockerClient;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testDeploymentState() {
        $(docker()
            .client(dockerClient)
            .inspectContainer("todo-app")
            .validateCommandResult((container, context) -> Assert.assertTrue(container.getState().getRunning())));
    }

    @Test
    @CitrusTest
    public void testTodoService() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

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
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }
}
