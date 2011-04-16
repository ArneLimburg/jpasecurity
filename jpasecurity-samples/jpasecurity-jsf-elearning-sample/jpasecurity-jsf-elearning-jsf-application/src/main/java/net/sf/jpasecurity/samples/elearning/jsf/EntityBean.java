/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.samples.elearning.jsf;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.jpasecurity.sample.elearning.domain.Entity;

/**
 * @author Arne Limburg
 */
public class EntityBean implements Entity {

    private int id;

    public EntityBean() {
        HttpServletRequest request
            = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        id = Integer.parseInt(request.getParameter("id"));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return getEntityInterface().getSimpleName() + " " + id;
    }

    private Class<?> getEntityInterface() {
        for (Class<?> iface: getClass().getInterfaces()) {
            if (Entity.class.isAssignableFrom(iface)) {
                return iface;
            }
        }
        return Entity.class;
    }
}
