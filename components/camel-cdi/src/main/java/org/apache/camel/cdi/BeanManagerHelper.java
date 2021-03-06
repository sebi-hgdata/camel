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
package org.apache.camel.cdi;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

@Vetoed
final class BeanManagerHelper {

    private BeanManagerHelper() {
    }

    static <T> Set<T> getReferencesByType(BeanManager manager, Class<T> type, Annotation... qualifiers) {
        Set<T> references = new HashSet<>();
        for (Bean<?> bean : manager.getBeans(type, qualifiers)) {
            references.add(getReference(manager, type, bean));
        }
        return references;
    }

    static <T> T getReferenceByName(BeanManager manager, String name, Class<T> type) {
        Set<Bean<?>> beans = manager.getBeans(name);
        if (beans == null || beans.isEmpty()) {
            return null;
        }
        return getReference(manager, type, manager.resolve(beans));
    }

    static <T> T getReferenceByType(BeanManager manager, Class<T> type, Annotation... qualifiers) {
        Set<Bean<?>> beans = manager.getBeans(type, qualifiers);
        if (beans == null || beans.isEmpty()) {
            return null;
        }
        return getReference(manager, type, manager.resolve(beans));
    }

    static <T> T getReference(BeanManager manager, Class<T> type, Bean<?> bean) {
        return type.cast(manager.getReference(bean, type, manager.createCreationalContext(bean)));
    }
}
