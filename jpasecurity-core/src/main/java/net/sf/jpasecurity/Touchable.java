/*
 * Copyright 2012 Arne Limburg
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
package net.sf.jpasecurity;

/**
 *
 * @author Arne Limburg
 */
public interface Touchable {

    /**
     * Determines, if {@link #touch()} was called since the last reset of the underlying object.
     */
    boolean isTouched();

    /**
     * A call to this method indicates that the object has been touched in some way (i.e. a method was called).
     */
    void touch();
}
