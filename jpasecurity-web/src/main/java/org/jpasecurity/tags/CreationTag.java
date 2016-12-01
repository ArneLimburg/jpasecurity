/*
 * Copyright 2009 Arne Limburg
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
package org.jpasecurity.tags;

import org.jpasecurity.AccessType;

/**
 * @author Arne Limburg
 */
public class CreationTag extends AbstractSecurityTag {

    private String type;
    private String parameters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object[] resolveParameters() {
        String[] parameterNames = parameters.split(",");
        Object[] resolvedParameters = new Object[parameterNames.length];
        for (int i = 0; i < resolvedParameters.length; i++) {
            Object parameter = pageContext.findAttribute(parameterNames[i].trim());
            if (parameter == null) {
                try {
                    resolvedParameters[i] = Integer.parseInt(parameterNames[i].trim());
                } catch (NumberFormatException n) {
                    try {
                        resolvedParameters[i] = Float.parseFloat(parameterNames[i].trim());
                    } catch (NumberFormatException e) {
                        resolvedParameters[i] = parameterNames[i].trim();
                    }
                }
            } else {
                resolvedParameters[i] = parameter;
            }
        }
        return resolvedParameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    protected boolean isAccessible() {
        return getAccessManager().isAccessible(AccessType.CREATE, getType(), resolveParameters());
    }
}
