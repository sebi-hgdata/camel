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
package org.apache.camel.management;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rest.DummyRestConsumerFactory;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.rest.RestParamType;

import java.util.Arrays;

public class ManagedFromRestGetTest extends ManagementTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("dummy-test", new DummyRestConsumerFactory());
        return new DefaultCamelContext(registry);
    }

    public void testFromRestModel() throws Exception {
        // JMX tests dont work well on AIX CI servers (hangs them)
        if (isPlatform("aix")) {
            return;
        }

        MBeanServer mbeanServer = getMBeanServer();

        ObjectName on = ObjectName.getInstance("org.apache.camel:context=camel-1,type=context,name=\"camel-1\"");

        String xml = (String) mbeanServer.invoke(on, "dumpRestsAsXml", null, null);
        assertNotNull(xml);
        log.info(xml);

        assertTrue(xml.contains("<rests"));
        assertTrue(xml.contains("<rest path=\"/say/hello\">"));
        assertTrue(xml.contains("<rest path=\"/say/bye\">"));
        assertTrue(xml.contains("</rest>"));
        assertTrue(xml.contains("<get>"));
        assertTrue(xml.contains("application/json"));
        assertTrue(xml.contains("<post>"));
        assertTrue(xml.contains("application/json"));
        assertTrue(xml.contains("</rests>"));

        assertTrue(xml.contains("<param paramType=\"query\" name=\"header_letter\" description=\"header param description2\" defaultValue=\"b\" required=\"false\" allowMultiple=\"true\" dataType=\"string\" paramAccess=\"acc2\">"));
        assertTrue(xml.contains("<param paramType=\"header\" name=\"header_count\" description=\"header param description1\" defaultValue=\"1\" required=\"true\" allowMultiple=\"false\" dataType=\"integer\" paramAccess=\"acc1\">"));
        assertTrue(xml.contains("<value>1</value>"));
        assertTrue(xml.contains("<value>a</value>"));

        String xml2 = (String) mbeanServer.invoke(on, "dumpRoutesAsXml", null, null);
        log.info(xml2);
        // and we should have rest in the routes that indicate its from a rest dsl
        assertTrue(xml2.contains("rest=\"true\""));

        // there should be 3 + 2 routes
        assertEquals(3 + 2, context.getRouteDefinitions().size());
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
                        .restParam().type(RestParamType.header).description("header param description1").dataType("integer").allowableValues(Arrays.asList("1", "2", "3", "4"))
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
