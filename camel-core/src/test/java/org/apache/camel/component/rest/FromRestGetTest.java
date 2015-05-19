/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.rest;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;

import java.util.Arrays;

public class FromRestGetTest extends ContextTestSupport {

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("dummy-rest", new DummyRestConsumerFactory());
        return jndi;
    }

    protected int getExpectedNumberOfRoutes() {
        return 2 + 3;
    }

    public void testFromRestModel() throws Exception {
        assertEquals(getExpectedNumberOfRoutes(), context.getRoutes().size());

        assertEquals(2, context.getRestDefinitions().size());
        RestDefinition rest = context.getRestDefinitions().get(0);
        assertNotNull(rest);
        assertEquals("/say/hello", rest.getPath());
        assertEquals(1, rest.getVerbs().size());
        ToDefinition to = assertIsInstanceOf(ToDefinition.class, rest.getVerbs().get(0).getTo());
        assertEquals("direct:hello", to.getUri());

        rest = context.getRestDefinitions().get(1);
        assertNotNull(rest);
        assertEquals("/say/bye", rest.getPath());
        assertEquals(2, rest.getVerbs().size());
        assertEquals("application/json", rest.getVerbs().get(0).getConsumes());

        assertEquals(2, rest.getVerbs().get(0).getParams().size());
        assertEquals(RestParamType.header, rest.getVerbs().get(0).getParams().get(0).getParamType());
        assertEquals(RestParamType.query, rest.getVerbs().get(0).getParams().get(1).getParamType());

        assertEquals("header param description1", rest.getVerbs().get(0).getParams().get(0).getDescription());
        assertEquals("header param description2", rest.getVerbs().get(0).getParams().get(1).getDescription());

        assertEquals("integer", rest.getVerbs().get(0).getParams().get(0).getDataType());
        assertEquals("string", rest.getVerbs().get(0).getParams().get(1).getDataType());
        assertEquals(Arrays.asList("1","2","3","4"), rest.getVerbs().get(0).getParams().get(0).getAllowableValues());
        assertEquals(Arrays.asList("a","b","c","d"), rest.getVerbs().get(0).getParams().get(1).getAllowableValues());
        assertEquals("1", rest.getVerbs().get(0).getParams().get(0).getDefaultValue());
        assertEquals("b", rest.getVerbs().get(0).getParams().get(1).getDefaultValue());

        assertEquals(Boolean.FALSE, rest.getVerbs().get(0).getParams().get(0).getAllowMultiple());
        assertEquals(Boolean.TRUE, rest.getVerbs().get(0).getParams().get(1).getAllowMultiple());

        assertEquals("header_count", rest.getVerbs().get(0).getParams().get(0).getName());
        assertEquals("header_letter", rest.getVerbs().get(0).getParams().get(1).getName());
        assertEquals(Boolean.TRUE, rest.getVerbs().get(0).getParams().get(0).getRequired());
        assertEquals(Boolean.FALSE, rest.getVerbs().get(0).getParams().get(1).getRequired());
        assertEquals("acc1", rest.getVerbs().get(0).getParams().get(0).getParamAccess());
        assertEquals("acc2", rest.getVerbs().get(0).getParams().get(1).getParamAccess());

        to = assertIsInstanceOf(ToDefinition.class, rest.getVerbs().get(0).getTo());
        assertEquals("direct:bye", to.getUri());

        // the rest becomes routes and the input is a seda endpoint created by the DummyRestConsumerFactory
        getMockEndpoint("mock:update").expectedMessageCount(1);
        template.sendBody("seda:post-say-bye", "I was here");
        assertMockEndpointsSatisfied();

        String out = template.requestBody("seda:get-say-hello", "Me", String.class);
        assertEquals("Hello World", out);
        String out2 = template.requestBody("seda:get-say-bye", "Me", String.class);
        assertEquals("Bye World", out2);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration().host("localhost");
                rest("/say/hello")
                    .get().to("direct:hello");

                rest("/say/bye")
                    .get().consumes("application/json")
                        .restParam().type(RestParamType.header).description("header param description1").dataType("integer").allowableValues(Arrays.asList("1","2","3","4"))
                            .defaultValue("1").allowMultiple(false).name("header_count").required(true).paramAccess("acc1")
                        .endParam().
                        restParam().type(RestParamType.query).description("header param description2").dataType("string").allowableValues(Arrays.asList("a","b","c","d"))
                            .defaultValue("b").allowMultiple(true).name("header_letter").required(false).paramAccess("acc2")
                        .endParam()
                        .to("direct:bye")
                    .post().to("mock:update");

                from("direct:hello")
                    .transform().constant("Hello World");

                from("direct:bye")
                    .transform().constant("Bye World");
            }
        };
    }
}
