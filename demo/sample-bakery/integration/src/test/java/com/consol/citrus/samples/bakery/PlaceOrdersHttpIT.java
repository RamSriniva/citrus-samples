/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.samples.bakery;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.functions.Functions;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static com.consol.citrus.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class PlaceOrdersHttpIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("bakeryClient")
    private HttpClient bakeryClient;

    @Autowired
    @Qualifier("reportingClient")
    private HttpClient reportingClient;

    @CitrusTest
    public void placeChocolateCookieOrder() {
        variable("orderId", Functions.randomNumber(10L, null));

        $(http()
            .client(bakeryClient)
            .send()
            .post("/order")
            .message()
            .type(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"order\": { \"type\": \"chocolate\", \"id\": ${orderId}, \"amount\": 1}}"));

        $(repeatOnError()
            .until((index, context) -> index > 20)
            .autoSleep(500L)
            .actions(
                http()
                    .client(reportingClient)
                    .send()
                    .get("/reporting/order")
                    .queryParam("id", "${orderId}"),
                http()
                    .client(reportingClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .message()
                    .type(MessageType.JSON)
                    .body("{\"status\": true}")
            ));

        $(http()
            .client(bakeryClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));
    }

    @CitrusTest
    public void placeCaramelCookieOrder() {
        variable("orderId", Functions.randomNumber(10L, null));

        $(http()
            .client(bakeryClient)
            .send()
            .post("/order")
            .message()
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"order\": { \"type\": \"caramel\", \"id\": ${orderId}, \"amount\": 1}}"));

        $(repeatOnError()
            .until((index, context) -> index > 20)
            .autoSleep(500L)
            .actions(
                http()
                    .client(reportingClient)
                    .send()
                    .get("/reporting/order")
                    .queryParam("id", "${orderId}"),
                http()
                    .client(reportingClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .message()
                    .type(MessageType.JSON)
                    .body("{\"status\": true}")
            ));

        $(http()
            .client(bakeryClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));
    }

    @CitrusTest
    public void placeBlueberryCookieOrder() {
        variable("orderId", Functions.randomNumber(10L, null));

        $(http()
            .client(bakeryClient)
            .send()
            .post("/order")
            .message()
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"order\": { \"type\": \"blueberry\", \"id\": ${orderId}, \"amount\": 1}}"));

        $(repeatOnError()
            .until((index, context) -> index > 20)
            .autoSleep(500L)
            .actions(
                http()
                    .client(reportingClient)
                    .send()
                    .get("/reporting/order")
                    .queryParam("id", "${orderId}"),
                http()
                    .client(reportingClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .message()
                    .type(MessageType.JSON)
                    .body("{\"status\": true}")
            ));

        $(http()
            .client(bakeryClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));
    }
}
