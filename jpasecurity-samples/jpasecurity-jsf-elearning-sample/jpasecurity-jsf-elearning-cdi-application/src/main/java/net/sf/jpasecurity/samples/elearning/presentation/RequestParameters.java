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
package net.sf.jpasecurity.samples.elearning.presentation;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.core.Parameter;

/**
 * @author Arne Limburg
 */
@RequestScoped
public class RequestParameters {

    private Map<String, String> requestParameters
        = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

    @Produces
    @Dependent
    @Parameter("any")
    public String getRequestParameterString(InjectionPoint injectionPoint) {
        return requestParameters.get(injectionPoint.getAnnotated().getAnnotation(Parameter.class).value());
    }

    @Produces
    @Dependent
    @Parameter("any")
    public Integer getRequestParameterInt(InjectionPoint injectionPoint) {
        String parameter = getRequestParameterString(injectionPoint);
        return parameter != null? Integer.parseInt(parameter): null;
    }
}
