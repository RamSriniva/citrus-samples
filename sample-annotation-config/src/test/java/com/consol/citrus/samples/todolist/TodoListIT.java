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

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.config.annotation.HttpClientConfig;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;
import static com.consol.citrus.validation.xml.XpathMessageValidationContext.Builder.xpath;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = { TodoAppAutoConfiguration.class })
public class TodoListIT extends TestNGCitrusSpringSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8080")
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testGet() {
        $(http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .message()
            .accept(MediaType.TEXT_HTML_VALUE));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .body("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                    "\"org/w3/xhtml/xhtml1-transitional.dtd\">" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                        "<head>@ignore@</head>" +
                        "<body>@ignore@</body>" +
                    "</html>")
            .validate(xpath()
                    .namespaceContext("xh", "http://www.w3.org/1999/xhtml")
                    .expression("//xh:h1", "TODO list")));
    }
}
