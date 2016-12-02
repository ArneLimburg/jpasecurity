/*
 * Copyright 2008 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.security;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.jpasecurity.AccessType.CREATE;
import static org.jpasecurity.AccessType.DELETE;
import static org.jpasecurity.AccessType.READ;
import static org.jpasecurity.AccessType.UPDATE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jpasecurity.AccessType;

/**
 * An annotation to provide an access rule for an entity.
 * <p>
 * Example:
 * <code>
 * \@Entity
 * \@Permit(rule = "owner = CURRENT_PRINCIPAL")
 * public class ExampleEntity {
 *   ...
 * }
 * </code>
 * @author Arne Limburg
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Permit {

    AccessType[] access() default { CREATE, READ, UPDATE, DELETE };
    String rule() default "";
}
